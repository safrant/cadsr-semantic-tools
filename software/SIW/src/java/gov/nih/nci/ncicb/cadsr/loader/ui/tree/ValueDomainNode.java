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
package gov.nih.nci.ncicb.cadsr.loader.ui.tree;

import gov.nih.nci.ncicb.cadsr.domain.ValueDomain;
import gov.nih.nci.ncicb.cadsr.domain.AlternateName;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ValueDomainNode 
  extends AbstractUMLNode<UMLNode> 
  implements ReviewableUMLNode
{

  static final Icon REVIEWED_ICON = 
    new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("tree-class-checked.gif"));

  static final Icon DEFAULT_ICON = 
    new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("tree-class.gif"));

  private boolean reviewed = false;

  public ValueDomainNode(ValueDomain vd) {
    for(AlternateName an : vd.getAlternateNames()) {
      if(an.getType().equals(AlternateName.TYPE_FULL_NAME))
        fullPath = an.getName();
    }
    if(fullPath == null)
      fullPath = vd.getLongName();

    int ind = fullPath.lastIndexOf(".");
    display = fullPath.substring(ind + 1);
    
    userObject = vd;
    
    icon = DEFAULT_ICON;
  }


  public void setReviewed(boolean currentStatus) 
  {
    reviewed = currentStatus;
    
    boolean changeIcon = true;
    
    // iterate over children
    // if all children are reviewed then review the class
    for(UMLNode l : getChildren())
      {
        ValueMeaningNode next = (ValueMeaningNode) l;
        if(!next.isReviewed()) 
        {
          changeIcon = false;
          break;
        }
      }
      
      if(changeIcon && currentStatus) 
      {
        setIcon(REVIEWED_ICON);
      } 
      else
        setIcon(DEFAULT_ICON);
        
      PackageNode parent = (PackageNode) getParent();
      parent.setReviewed(parent.isReviewed());
  }
  
  public boolean isReviewed() 
  {
    return reviewed;
  }

}