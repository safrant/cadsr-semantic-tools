package gov.nih.nci.ncicb.cadsr.loader.persister;

import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;

import gov.nih.nci.ncicb.cadsr.domain.*;
import gov.nih.nci.ncicb.cadsr.dao.*;

import gov.nih.nci.ncicb.cadsr.spring.*;

import java.util.*;

import org.apache.log4j.Logger;

public class UMLPersister implements Persister {

  private ElementsLists elements = null;

  private AdminComponentDAO adminComponentDAO;
  private DataElementDAO dataElementDAO;
  private ContextDAO contextDAO;
  private DataElementConceptDAO dataElementConceptDAO;
  private ValueDomainDAO valueDomainDAO; 
  private ConceptualDomainDAO conceptualDomainDAO;
  private PropertyDAO propertyDAO;
  private ObjectClassDAO objectClassDAO;
  private ClassificationSchemeDAO classificationSchemeDAO;
  private ClassificationSchemeItemDAO classificationSchemeItemDAO;

  private LoaderDAO loaderDAO;

  private HashMap params = new HashMap();
  
  private String projectName, projectVersion, version, workflowStatus;
  private Context context;
  private ConceptualDomain conceptualDomain;

  private ClassificationSchemeItem domainCsi;
  private ClassificationScheme projectCs;
  private ClassSchemeClassSchemeItem projectCsCsi;

  private HashMap valueDomains = new HashMap();

  private Logger logger;

  public UMLPersister(ElementsLists list) {

    System.setProperty("log4j.configuration", "uml-log4j.properties");
    logger  = Logger.getLogger(UMLPersister.class.getName());

    this.elements = list;

    ApplicationContextFactory.init("applicationContext.xml");

    logger.debug("Loading ContextDAO bean");
    System.out.println("Loading ContextDAO bean");
    contextDAO = (ContextDAO) ApplicationContextFactory.getApplicationContext().getBean("contextDAO");


    System.out.println("Loading DataElementDAO bean");
    dataElementDAO = (DataElementDAO) ApplicationContextFactory.getApplicationContext().getBean("dataElementDAO");

    System.out.println("Loading AdminComponentDAO bean");
    adminComponentDAO = (AdminComponentDAO) ApplicationContextFactory.getApplicationContext().getBean("adminComponentDAO");

    System.out.println("Loading DataElementConceptDAO bean");
    dataElementConceptDAO = (DataElementConceptDAO) ApplicationContextFactory.getApplicationContext().getBean("dataElementConceptDAO");

    System.out.println("Loading CDDAO bean");
    conceptualDomainDAO = (ConceptualDomainDAO) ApplicationContextFactory.getApplicationContext().getBean("conceptualDomainDAO");

    System.out.println("Loading VDDAO bean");
    valueDomainDAO = (ValueDomainDAO) ApplicationContextFactory.getApplicationContext().getBean("valueDomainDAO");

    System.out.println("Loading PropertyDAO bean");
    propertyDAO = (PropertyDAO) ApplicationContextFactory.getApplicationContext().getBean("propertyDAO");

    System.out.println("Loading ObjectClassDAO bean");
    objectClassDAO = (ObjectClassDAO) ApplicationContextFactory.getApplicationContext().getBean("objectClassDAO");

    System.out.println("Loading CSDAO bean");
    classificationSchemeDAO = (ClassificationSchemeDAO) ApplicationContextFactory.getApplicationContext().getBean("classificationSchemeDAO");

    System.out.println("Loading CSIDAO bean");
    classificationSchemeItemDAO = (ClassificationSchemeItemDAO) ApplicationContextFactory.getApplicationContext().getBean("classificationSchemeItemDAO");

    System.out.println("Loading LoaderDAO bean");
    loaderDAO = (LoaderDAO) ApplicationContextFactory.getApplicationContext().getBean("loaderDAO");

  }

  public void setParameter(String key, Object value) {
    params.put(key, value);
  }

  public void persist() throws PersisterException {

    initParams();

    initClassifications();

    persistProperties();
    
    persistObjectClasses();

    persistDecs();

    persistDes();

  }


  public void persistDes() throws PersisterException {
    DataElement de = DomainObjectFactory.newDataElement();
    List des = (List)elements.getElements(de.getClass());
    
    if(des != null)
      for(ListIterator it=des.listIterator(); it.hasNext(); ) {
	de = (DataElement)it.next();
	
	de.setContext(context);

	int ind = de.getLongName().lastIndexOf(".");
	if(ind > 0)
	  de.setLongName(de.getLongName().substring(ind+1));

	List decs = elements.getElements(DomainObjectFactory.newDataElementConcept().getClass());
	for(ListIterator lit=decs.listIterator(); lit.hasNext();) {
	  DataElementConcept o = (DataElementConcept)lit.next();
	  if(o.getLongName().equals(de.getDataElementConcept().getLongName()))
	    de.setDataElementConcept(o);
	}

	de.setValueDomain(lookupValueDomain(de.getValueDomain()));
	List l = dataElementDAO.find(de);
	if(l.size() == 0) {
	  de.setPreferredDefinition(de.getLongName());
	  de.setPreferredName(de.getLongName());
	  // !!!!! TODO -- following will pass constraints
	  if(de.getPreferredName().length() > 30)
	    de.setPreferredName(de.getPreferredName().substring(0, 29));

	  de.setVersion(new Float(version));
	  
	  de.setWorkflowStatus(workflowStatus);

	  de.setId(dataElementDAO.create(de));
	} else {
	  de = (DataElement)l.get(0);
	}	

	addClassificationSchemes(de);
	it.set(de);

      }
  }

  // !!!! TODO 
  // EVS CONCEPT CODE.
  private void persistProperties() throws PersisterException {

    Property prop = DomainObjectFactory.newProperty();
    List props = (List)elements.getElements(prop.getClass());

    if(props != null)
      for(ListIterator it=props.listIterator(); it.hasNext();) {
	prop = (Property)it.next();

	prop.setContext(context);
	
	// does this property exist?
	List l = propertyDAO.find(prop);
	if(l.size() == 0) {
	  // !!!!! TODO
	  prop.setPreferredDefinition(prop.getLongName());
	  prop.setPreferredName(prop.getLongName());
	  
	  prop.setVersion(new Float(1.0f));
	  
	  prop.setWorkflowStatus(workflowStatus);
	  prop.setId(propertyDAO.create(prop));
	} else {
	  prop = (Property)l.get(0);
	}	
	
	addClassificationSchemes(prop);
	it.set(prop);
      }

  }

  
  private void persistObjectClasses() throws PersisterException {

    ObjectClass oc = DomainObjectFactory.newObjectClass();
    List ocs = (List)elements.getElements(oc.getClass());

    System.out.println("ocs...");
    
    if(ocs != null)
      for(ListIterator it=ocs.listIterator(); it.hasNext(); ) {
	oc = (ObjectClass)it.next();
	oc.setContext(context);

	String className = oc.getLongName();
	int ind = className.lastIndexOf(".");
	String packageName = className.substring(0, ind - 1);
	className = className.substring(ind + 1);
	
	oc.setLongName(className);
	// does this oc exist?
	List l = objectClassDAO.find(oc);
	if(l.size() == 0) {
	  // !!!!! TODO
	  oc.setPreferredDefinition(oc.getLongName());
	  oc.setPreferredName(oc.getLongName());

	  oc.setVersion(new Float(1.0f));

	  oc.setWorkflowStatus(workflowStatus);
	  oc.setId(objectClassDAO.create(oc));
	} else {
	  oc = (ObjectClass)l.get(0);
	}	

	addClassificationSchemes(oc);
	it.set(oc);

	// add designation to hold package name
	// !!!! TODO
	
      }

  }

  private void persistDecs() throws PersisterException {
    DataElementConcept dec = DomainObjectFactory.newDataElementConcept();
    List decs = (List)elements.getElements(dec.getClass());
    
    System.out.println("decs: " + decs.size());

    if(decs != null)
      for(ListIterator it=decs.listIterator(); it.hasNext(); ) {
	dec = (DataElementConcept)it.next();
	
	dec.setContext(context);
	dec.setConceptualDomain(conceptualDomain);

	int ind = dec.getLongName().lastIndexOf(".");
	if(ind > 0)
	  dec.setLongName(dec.getLongName().substring(ind+1));

	System.out.println("dec name: " + dec.getLongName());

	// does this dec exist?
	List l = dataElementConceptDAO.find(dec);
	if(l.size() == 0) {
	  // !!!!! TODO
	  dec.setPreferredDefinition(dec.getLongName());
	  dec.setPreferredName(dec.getLongName());

	  dec.setVersion(new Float(version));

	  dec.setWorkflowStatus(workflowStatus);

	  List ocs = elements.getElements(DomainObjectFactory.newObjectClass().getClass());
	  for(int j=0; j<ocs.size(); j++) {
	    ObjectClass o = (ObjectClass)ocs.get(j);
	    if(o.getLongName().equals(dec.getObjectClass().getLongName()))
	      dec.setObjectClass(o);
	  }

	  List props = elements.getElements(DomainObjectFactory.newProperty().getClass());
	  for(int j=0; j<props.size(); j++) {
	    Property o = (Property)props.get(j);
	    if(o.getLongName().equals(dec.getProperty().getLongName()))
	      dec.setProperty(o);
	  }

	  
	  dec.setId(dataElementConceptDAO.create(dec));
	} else {
	  dec = (DataElementConcept)l.get(0);
	}	

	addClassificationSchemes(dec);
	it.set(dec);

	// add designation to hold package name
	// !!!! TODO
	
      }

  }


  private void addClassificationSchemes(AdminComponent ac) throws PersisterException {

    // Add Classification Schemes
    List l = adminComponentDAO.getClassSchemeClassSchemeItems(ac);
    
    // is projectCs linked?
    boolean found = false;

    for(ListIterator it = l.listIterator(); it.hasNext();) {
      ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem)it.next();

      if(csCsi.getCs().getLongName().equals(projectCs.getLongName()))
	if(csCsi.getCsi().getName().equals(domainCsi.getName()))
	  found = true;
      
     
    } 
    List csCsis = new ArrayList();
    if(!found) {
      csCsis.add(projectCsCsi);
      adminComponentDAO.addClassSchemeClassSchemeItems(ac, csCsis);
    }
    
  }
  
  private void initParams() throws PersisterException {

    projectName = (String)params.get("projectName");
//     LoaderDefault loaderDefault = loaderDAO.findDefaults(defaultsId);
    LoaderDefault loaderDefault = loaderDAO.findByName(projectName);

    if(loaderDefault == null)
      throw new PersisterException("Defaults not found. Please create a profile first.");
    
    String cName = loaderDefault.getContextName();
    if(cName == null)
      throw new PersisterException("Context Name not Set.");

    context = contextDAO.findByName(cName);
    if(context == null)
      throw new PersisterException("Context: " + cName + " not found.");

    version = loaderDefault.getVersion().toString();
    projectVersion = loaderDefault.getProjectVersion().toString();

    workflowStatus = loaderDefault.getWorkflowStatus();
    if(workflowStatus == null)
      throw new PersisterException("WorkflowStatus not Set.");

    
    conceptualDomain = DomainObjectFactory.newConceptualDomain();
    conceptualDomain.setPreferredName(loaderDefault.getCdName());
    
    Context cdContext = contextDAO.findByName(loaderDefault.getCdContextName());
    if(cdContext == null)
      throw new PersisterException("CD Context not found.");


    conceptualDomain.setContext(cdContext);

    try {
      conceptualDomain = (ConceptualDomain)conceptualDomainDAO.find(conceptualDomain).get(0);
    } catch (NullPointerException e){
      throw new PersisterException("CD: " + conceptualDomain.getPreferredName() + " not found.");
    } 
    

  }
  
  private void initClassifications() throws PersisterException {

    domainCsi = DomainObjectFactory.newClassificationSchemeItem();
    domainCsi.setName("Essai-Domain Model");
    // !!!! TODO
    domainCsi.setType("TEST");
//     ArrayList eager = new ArrayList();
//     eager.add(EagerConstants.CS_CSI);
    List result = classificationSchemeItemDAO.find(domainCsi);
    if(result.size() == 0)
      throw new PersisterException("Classification Scheme Item: " + domainCsi.getName() + " does not exist on DB.");
    domainCsi = (ClassificationSchemeItem)result.get(0);


    projectCs = DomainObjectFactory.newClassificationScheme();
    projectCs.setLongName(projectName);
    projectCs.setVersion(new Float(projectVersion));
    projectCs.setContext(context);
    ArrayList eager = new ArrayList();
    eager.add(EagerConstants.CS_CSI);
    result = classificationSchemeDAO.find(projectCs, eager);
    
    if(result.size() == 0) { // need to add projectName CS
      projectCs.setPreferredName(projectName);
      projectCs.setWorkflowStatus(workflowStatus);
      projectCs.setPreferredDefinition("Un essai de CS. Nom du projet.");
      projectCs.setType("TEST");
      projectCs.setLabelType(ClassificationScheme.LABEL_TYPE_ALPHA);

      projectCs.setId(classificationSchemeDAO.create(projectCs));
      projectCsCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
      projectCsCsi.setCs(projectCs);
      projectCsCsi.setCsi(domainCsi);
      projectCsCsi.setLabel(projectName);
      
      classificationSchemeDAO.addClassificationSchemeItem(projectCs, projectCsCsi);
    } else { // is domainCsi linked?
      projectCs = (ClassificationScheme)result.get(0);
      List csCsis = projectCs.getCsCsis();
      boolean found = false;
      for(ListIterator it = csCsis.listIterator(); it.hasNext(); ) 
	{
	  ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem)it.next();
	  if(csCsi.getCsi().getName().equals(domainCsi.getName())) 
	    {
	      projectCsCsi = csCsi;
	      found = true;
	    }
	}
      if(!found) {
	projectCsCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
	projectCsCsi.setCs(projectCs);
	projectCsCsi.setCsi(domainCsi);
	projectCsCsi.setLabel(projectName);

	classificationSchemeDAO.addClassificationSchemeItem(projectCs, projectCsCsi);
      }
      

//     if(projectCsCsi == null) { // need to add projectName CSI
//       ClassificationSchemeItem csi = DomainObjectFactory.newClassificationSchemeItem();
//       csi.setName(projectName);
//       csi.setType("TEST");

//       ClassSchemeClassSchemeItem csCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
//       csCsi.setCs(domainCs);
//       csCsi.setCsi(csi);
//       csCsi.setLabel(projectName);

//       csCsi.setId((String)classificationSchemeDAO.addClassificationSchemeItem(domainCs, csCsi));

//       List list = classificationSchemeItemDAO.find(csi);
//       projectCsi = (ClassificationSchemeItem)list.get(0);
      
//       projectCsCsi = csCsi;

//     }


//     for(int i=0; i<csCsis.size(); i++) {      
//       ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem)csCsis.get(i);
//       if(csCsi.getCsi().getName().equals(projectName + "-" + version)) {
// 	versionCsCsi = csCsi;
// 	versionCsi = versionCsCsi.getCsi();
//       }
//     }
//     if(versionCsCsi == null) { // need to add version CSI
//       ClassificationSchemeItem csi = DomainObjectFactory.newClassificationSchemeItem();
//       csi.setName(projectName + "-" + version);
//       csi.setType("TEST");

//       ClassSchemeClassSchemeItem csCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
//       csCsi.setCs(domainCs);
//       csCsi.setCsi(csi);
//       csCsi.setLabel(csi.getName());
      
//       csCsi.setId((String)classificationSchemeDAO.addClassificationSchemeItem(domainCs, csCsi));

//       List list = classificationSchemeItemDAO.find(csi);
//       versionCsi = (ClassificationSchemeItem)list.get(0);

//       versionCsCsi = csCsi;

//     }

    }
  }
  
  private ValueDomain lookupValueDomain(ValueDomain vd) throws PersisterException {
    ValueDomain result = (ValueDomain)valueDomains.get(vd.getPreferredName());

    if(result == null) { // not in cache -- go to db
      result = (ValueDomain)valueDomainDAO.find(vd).get(0);
      valueDomains.put(result.getPreferredName(), result);
    }

    return result;

  }

  
}
