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

package gov.nih.nci.ncicb.cadsr.loader;

import java.util.*;

import org.omg.uml.foundation.core.*;
import org.omg.uml.foundation.extensionmechanisms.*;

import org.omg.uml.modelmanagement.Model;
import org.omg.uml.modelmanagement.UmlPackage;

import java.io.*;

import gov.nih.nci.ncicb.cadsr.loader.event.*;
import gov.nih.nci.ncicb.cadsr.loader.parser.*;
import gov.nih.nci.ncicb.cadsr.loader.persister.*;
import gov.nih.nci.ncicb.cadsr.loader.validator.*;
import gov.nih.nci.ncicb.cadsr.loader.util.DAOAccessor;
import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;

import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;

import java.security.*;
import javax.security.auth.*;
import javax.security.auth.login.*;
import javax.security.auth.callback.CallbackHandler;


import org.apache.log4j.Logger;

import gov.nih.nci.ncicb.cadsr.jaas.ConsoleCallbackHandler;


/**
 *
 * <code>UMLLoader</code> is the starting class for running UML Loader un command line. <br/>
 * Usage: UMLLoader dir-name project-name
 * <ul><li>dir-name is the full path to the directory containing the XMI files</li>
 * <li>project-name is the name of an existing project in the UML_LOADER_DEFAULTS table of CADSR.</li>
 * </ul>
 * In order to start UML Loader, one needs a 'defaults' record in CADSR. 
 *
 * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
 * 
 */
public class UMLLoader {

  private static Logger logger = Logger.getLogger(UMLLoader.class.getName());

  /**
   *
   * @param args a <code>String[]</code> value
   * @exception Exception if an error occurs
   */
  public static void main(String[] args) throws Exception {

    if(args.length != 3) {
      System.err.println(PropertyAccessor.getProperty("usage"));
      System.exit(1);
    }

    new UMLLoader().run(args);
  }

  private void run(String[] args) throws Exception {
    InitClass initClass = new InitClass(this);
    Thread t = new Thread(initClass);
    /* high priority because:
     * If the user is fast at entering its user name
     * (namely, username / password is provided automatically)
     * Then it's possible for the login module to want to access
     *      spring before it's initialized. 
     * Would not happen in normal run, but in dev runs, it may.
     * This seems to have absolutely no effect (on linux at least). Will investigate later. 
     */
    t.setPriority(Thread.MAX_PRIORITY);
    t.start();

    try {
      Thread.currentThread().sleep(500);
    } catch (Exception e){
    } // end of try-catch

    String[] filenames = new File(args[0]).list(new FilenameFilter() {
	public boolean accept(File dir, String name) {
	  return name.endsWith(".xmi");
	}
      });

    if(filenames == null) 
      filenames = new String[0];
    
    if(filenames.length == 0) {
      logger.info(PropertyAccessor.getProperty("no.files"));
      System.exit(0);
    }

    LoginContext lc = new LoginContext("UML_Loader", new ConsoleCallbackHandler());

    String username = null;
    
    try {
      lc.login();
      boolean loginSuccess = true;
      
      Subject subject = lc.getSubject();

      Iterator it = subject.getPrincipals().iterator();
      while (it.hasNext()) {
	username = it.next().toString();
	logger.debug(PropertyAccessor.getProperty("authenticated", username));
      }
    } catch (Exception ex) {
      logger.error(PropertyAccessor.getProperty("login.fail",ex.getMessage()));
      System.exit(1);
    }
    
    String projectName = args[1];

    Float projectVersion = null;
    try {
      projectVersion = new Float(args[2]);
    } catch (NumberFormatException ex) {
      System.err.println("Parameter projectVersion must be a number");
      System.exit(1);
    }
    
    logger.info(PropertyAccessor.getProperty("nbOfFiles", filenames.length));
    
    Validator validator = new UMLValidator();
    UMLHandler listener = new UMLDefaultHandler(ElementsLists.getInstance());

    synchronized(initClass) {
      if(!initClass.isDone())
        try {
          wait();
        } catch (Exception e){
        } // end of try-catch
    }
    
    for(int i=0; i<filenames.length; i++) {
      logger.info(PropertyAccessor.getProperty("startingFile", filenames[i]));

      UMLDefaults defaults = UMLDefaults.getInstance();
      defaults.initParams(projectName, projectVersion, username);
//       defaults.initClassifications();

      XMIParser  parser = new XMIParser();
      parser.setEventHandler(listener);
      parser.parse(args[0] + "/" + filenames[i]);
      
    }

    ValidationItems items = validator.validate();
    Set errors = items.getErrors();
    if(errors.size() > 0) {
      // Ask user if we should continue
      for(Iterator it=errors.iterator(); it.hasNext();) {
        ValidationError error = (ValidationError)it.next();
        // !!! TODO choose error, warning, etc ...
        logger.error("ERROR: " + error.getMessage());
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.print(PropertyAccessor.getProperty("validation.continue"));
      String answ = br.readLine();
      if(!answ.equals("y")) {
        System.exit(1);
      }
    }

    Set warnings = items.getErrors();
    if(warnings.size() > 0) {
      // Ask user if we should continue
      for(Iterator it=warnings.iterator(); it.hasNext();) {
        ValidationWarning warning = (ValidationWarning)it.next();
        // !!! TODO choose error, warning, etc ...
        logger.error("ERROR: " + warning.getMessage());
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.print(PropertyAccessor.getProperty("validation.continue"));
      String answ = br.readLine();
      if(!answ.equals("y")) {
        System.exit(1);
      }
    }

    Persister persister = new UMLPersister(ElementsLists.getInstance());
    persister.persist();

  }

  /**
   * <code>UMLLoader</code> starts a separate thread for initializing the environement, while users enter their username and password. 
   *
   */
  class InitClass implements Runnable {
    Object parent;
    boolean done = false;

    InitClass(Object parent) {
      this.parent = parent;
    }
    
    public void run() {
      DAOAccessor p = new DAOAccessor();
      synchronized (this) {
	done = true;
	notifyAll();
      }
    }
    
    public boolean isDone() {
      return done;
    }


  }

}

