package gov.nih.nci.ncicb.cadsr.loader.event;

import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;

import gov.nih.nci.ncicb.cadsr.domain.*;

import java.util.List;

/**
 * UML Loader Handler interface
 *
 * @author <a href="mailto:ludetc@mail.nih.gov">Christophe Ludet</a>
 */
public interface UMLHandler extends LoaderHandler {

  public void newPackage(NewPackageEvent event);
  public void newOperation(NewOperationEvent event);
  public void newClass(NewClassEvent event);
  public void newAttribute(NewAttributeEvent event);
  public void newInterface(NewInterfaceEvent event);
  public void newStereotype(NewStereotypeEvent event);
  public void newDataType(NewDataTypeEvent event);
  public void newAssociation(NewAssociationEvent event);
  public void newGeneralization(NewGeneralizationEvent event);

}