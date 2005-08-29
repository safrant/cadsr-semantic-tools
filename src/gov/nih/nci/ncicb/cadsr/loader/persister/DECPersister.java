/*
 * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 *
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
 *
 * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
 *
 * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
 *
 * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
 *
 * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
 *
 * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 */
package gov.nih.nci.ncicb.cadsr.loader.persister;

import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
import gov.nih.nci.ncicb.cadsr.dao.*;
import gov.nih.nci.ncicb.cadsr.domain.*;
import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;

import org.apache.log4j.Logger;

import java.util.*;

/**
 *
 * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
 */
public class DECPersister extends UMLPersister {

  public static String DEC_PREFERRED_NAME_DELIMITER = "v";
  public static String DEC_PREFERRED_NAME_CONCAT_CHAR = ":";

  public static String DEC_PREFERRED_DEF_CONCAT_CHAR = "_";

  private static Logger logger = Logger.getLogger(DECPersister.class.getName());

  public DECPersister(ElementsLists list) {
    this.elements = list;
    defaults = UMLDefaults.getInstance();
  }

  public void persist() throws PersisterException {
    DataElementConcept dec = DomainObjectFactory.newDataElementConcept();
    List decs = (List) elements.getElements(dec.getClass());

    logger.debug("decs... ");
    if (decs != null) {
      for (ListIterator it = decs.listIterator(); it.hasNext();) {
        DataElementConcept newDec = DomainObjectFactory.newDataElementConcept();
	dec = (DataElementConcept) it.next();
        
        String packageName = getPackageName(dec);
        
        // update object class with persisted one
        dec.setObjectClass(lookupObjectClass(
                             dec.getObjectClass().getPreferredName()));
        newDec.setObjectClass(dec.getObjectClass());

        // update object class with persisted one
        dec.setProperty(lookupProperty(
                             dec.getProperty().getPreferredName()));
        newDec.setProperty(dec.getProperty());
        
        String newName = dec.getLongName();

	logger.debug("dec name: " + dec.getLongName());
        logger.debug("alt Name: " + newName);

	// does this dec exist?
	List l = dataElementConceptDAO.find(newDec);

//         String newDef = dec.getPreferredDefinition();
	if (l.size() == 0) {
          dec.setConceptualDomain(defaults.getConceptualDomain());
          dec.setContext(defaults.getContext());
          dec.setLongName(
            dec.getObjectClass().getLongName()
            + " " + 
            dec.getProperty().getLongName()
            );
	  dec.setPreferredDefinition(
            dec.getObjectClass().getPreferredDefinition()
            + DEC_PREFERRED_DEF_CONCAT_CHAR 
            + dec.getProperty().getPreferredDefinition()
            
            );

	  dec.setPreferredName(
            dec.getObjectClass().getPublicId() 
            + DEC_PREFERRED_NAME_DELIMITER
            + dec.getObjectClass().getVersion()
            + DEC_PREFERRED_NAME_CONCAT_CHAR
            + dec.getProperty().getPublicId()
            + DEC_PREFERRED_NAME_DELIMITER
            + dec.getProperty().getVersion());

	  dec.setVersion(new Float(1.0f));
	  dec.setWorkflowStatus(defaults.getWorkflowStatus());

	  List props = elements.getElements(DomainObjectFactory.newProperty()
					    .getClass());

	  for (int j = 0; j < props.size(); j++) {
	    Property o = (Property) props.get(j);

	    if (o.getLongName().equals(dec.getProperty()
				       .getLongName())) {
	      dec.setProperty(o);
	    }
	  }

	  dec.setAudit(defaults.getAudit());

          List altDefs = new ArrayList(dec.getDefinitions());
          List altNames = new ArrayList(dec.getAlternateNames());

          newDec = dataElementConceptDAO.create(dec);

          // restore altNames
          for(Iterator it2 = altNames.iterator(); it2.hasNext();) {
            AlternateName an = (AlternateName)it2.next();
            dec.addAlternateName(an);
          }
          // restore altDefs
          for(Iterator it2 = altDefs.iterator(); it2.hasNext();) {
            Definition def = (Definition)it2.next();
            dec.addDefinition(def);
          }

	  logger.info(PropertyAccessor.getProperty("created.dec"));

	} else {
	  newDec = (DataElementConcept) l.get(0);
	  logger.info(PropertyAccessor.getProperty("existed.dec"));

          /* if DEC alreay exists, check context
           * If context is different, add Used_by alt_name
           */
          if(!newDec.getContext().getId().equals(defaults.getContext().getId())) {
            addAlternateName(newDec, defaults.getContext().getName(), AlternateName.TYPE_USED_BY, null);
          }
          

	}

        addAlternateName(newDec, newName, AlternateName.TYPE_UML_DEC, packageName);

        for(Iterator it2 = dec.getDefinitions().iterator(); it2.hasNext(); ) {
          Definition def = (Definition)it2.next();
          addAlternateDefinition(
            newDec, def.getDefinition(), 
            def.getType(), packageName);
        }

	LogUtil.logAc(newDec, logger);
        logger.info("-- Public ID: " + newDec.getPublicId());
	logger.info(PropertyAccessor
                    .getProperty("oc.longName",
                                 newDec.getObjectClass().getLongName()));
	logger.info(PropertyAccessor
                    .getProperty("prop.longName",
                                 newDec.getProperty().getLongName()));


        addPackageClassification(newDec, packageName);
	it.set(newDec);

        // dec still referenced in DE. Need ID to retrieve it in DEPersister.
        dec.setId(newDec.getId());        

      }
    }

  }


}
