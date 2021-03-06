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
package gov.nih.nci.ncicb.cadsr.loader.event;

/**
 * Used by UMLLoader's parser to indicate a new UML Class event.
 *
 * @author <a href="mailto:ludetc@mail.nih.gov">Christophe Ludet</a>
 */
public class NewClassEvent extends NewConceptualEvent {

  private String name;
  private String packageName;

  private String gmeNamespace;
  private String gmeXmlElement;

  private boolean excludeFromSemanticInheritance = false;
  private String excludeFromSemanticInheritanceReason = null;

  public NewClassEvent(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /**
   * Get the PackageName value.
   * @return the PackageName value.
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Set the PackageName value.
   * @param newPackageName The new PackageName value.
   */
  public void setPackageName(String newPackageName) {
    this.packageName = newPackageName;
  }


  /**
   * Get the GmeNamespace value.
   * @return the GmeNamespace value.
   */
  public String getGmeNamespace() {
    return gmeNamespace;
  }

  /**
   * Set the GmeNamespace value.
   * @param newGmeNamespace The new GmeNamespace value.
   */
  public void setGmeNamespace(String newGmeNamespace) {
    this.gmeNamespace = newGmeNamespace;
  }


  /**
   * Get the GmeXmlElement value.
   * @return the GmeXmlElement value.
   */
  public String getGmeXmlElement() {
    return gmeXmlElement;
  }

  /**
   * Set the GmeXmlElement value.
   * @param newGmeXmlElement The new GmeXmlElement value.
   */
  public void setGmeXmlElement(String newGmeXmlElement) {
    this.gmeXmlElement = newGmeXmlElement;
  }


  /**
   * Get the ExcludeFromSemanticInheritanceReason value.
   * @return the ExcludeFromSemanticInheritanceReason value.
   */
  public String getExcludeFromSemanticInheritanceReason() {
    return excludeFromSemanticInheritanceReason;
  }

  /**
   * Set the ExcludeFromSemanticInheritanceReason value.
   * @param newExcludeFromSemanticInheritanceReason The new ExcludeFromSemanticInheritanceReason value.
   */
  public void setExcludeFromSemanticInheritanceReason(String newExcludeFromSemanticInheritanceReason) {
    this.excludeFromSemanticInheritanceReason = newExcludeFromSemanticInheritanceReason;
  }


  /**
   * Get the ExcludeFromSemanticInheritance value.
   * @return the ExcludeFromSemanticInheritance value.
   */
  public boolean isExcludeFromSemanticInheritance() {
    return excludeFromSemanticInheritance;
  }

  /**
   * Set the ExcludeFromSemanticInheritance value.
   * @param newExcludeFromSemanticInheritance The new ExcludeFromSemanticInheritance value.
   */
  public void setExcludeFromSemanticInheritance(boolean newExcludeFromSemanticInheritance) {
    this.excludeFromSemanticInheritance = newExcludeFromSemanticInheritance;
  }

  
 
}