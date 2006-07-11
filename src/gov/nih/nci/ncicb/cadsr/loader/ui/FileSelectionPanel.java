/*
 * Copyright 2000-2005 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
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
package gov.nih.nci.ncicb.cadsr.loader.ui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;


import java.io.File;

import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
import gov.nih.nci.ncicb.cadsr.loader.util.*;
import gov.nih.nci.ncicb.cadsr.loader.event.*;
import gov.nih.nci.ncicb.cadsr.loader.ui.event.*;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class FileSelectionPanel extends JPanel 
implements ProgressListener {

  public static final int SELECTION_NEW = 1;
  public static final int SELECTION_CONTINUE = 2;
  public static final int SELECTION_CANCEL = -1;

  private JButton newButton, continueButton, browseButton;
  private JLabel newLabel, continueLabel;

  private JTextField filePathField;

  private JPanel _this = this;

  private ProgressPanel progressPanel;
  private boolean isProgress = false;
  private int goal;

  private RunMode runMode = null;
  private String fileExtension = "xmi";

  private JCheckBox skipVdValidationCheckBox = 
    new JCheckBox("Skip Value Domain Validation");
  
  private List<ActionListener> actionListeners = new ArrayList();

  public FileSelectionPanel()
  {
      
  }

  public void init() {
    UserSelections selections = UserSelections.getInstance();
    
    runMode = (RunMode)(selections.getProperty("MODE"));
    if(runMode.equals(RunMode.Curator)) 
      fileExtension = "csv";
    else if(runMode.equals(RunMode.Reviewer)) 
      fileExtension = "xmi";
    else if(runMode.equals(RunMode.GenerateReport)) 
      fileExtension = "xmi";
    else if(runMode.equals(RunMode.AnnotateXMI))
      fileExtension = "xmi";
    initUI();
  }

  public FileSelectionPanel(int progressGoal)
  {
    this.isProgress = true;
    this.goal = progressGoal;
    initUI();
  }

  public void newProgressEvent(ProgressEvent evt) {
    progressPanel.newProgressEvent(evt);
  }

  public String getSelection() {
    String s = filePathField.getText().replace('\\', '/');
    return s;
  }

  public boolean getSkipVdValidation() {
    return skipVdValidationCheckBox.isSelected();
  }

  public void addFileActionListener(ActionListener l) {
    browseButton.addActionListener(l);
  }

  private void initUI() {

    this.removeAll();

    this.setLayout(new BorderLayout());
    
    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    infoPanel.setBackground(Color.WHITE);
    infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    infoPanel.add(new JLabel(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("siw-logo3_2.gif"))));

    JLabel infoLabel = new JLabel("<html>Please choose a file to parse<br>The file must be in " + fileExtension.toUpperCase() + " format</html>");
    infoPanel.add(infoLabel);
    
    this.add(infoPanel, BorderLayout.NORTH);
    
    browseButton = new JButton("Browse");
    filePathField = new JTextField(30);
    
    filePathField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        fireActionEvent(event);
      }
      });
    filePathField.addKeyListener(new KeyAdapter() {
        public void keyReleased(KeyEvent evt) {
          fireActionEvent(null);
        }
      });

    JPanel browsePanel = new JPanel();

    browsePanel.setLayout(new GridBagLayout());

    insertInBag(browsePanel, 
                new JLabel("Click browse to search for an " + fileExtension.toUpperCase() + " file"), 0, 0, 2, 1);

    insertInBag(browsePanel, filePathField, 0, 1);
    insertInBag(browsePanel, browseButton, 1, 1);

    insertInBag(browsePanel, 
                new JLabel("Recent Files:"), 0, 2, 2, 1);


    final UserPreferences prefs = UserPreferences.getInstance();

    final java.util.List<String> recentFiles = prefs.getRecentFiles();
    
    int y = 3, z=0;
    for(String s : recentFiles) {
      final String fileStr = new File(s).getName();
      
      final JLabel jl = new JLabel(fileStr);
      
      Font f = jl.getFont();
      Font newFont = new Font(f.getName(), f.getStyle(), f.getSize() - 2);
      jl.setFont(newFont);

      final int index = z++;
      jl.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
            filePathField.setText(recentFiles.get(index));
            fireActionEvent(null);
          }
      });
      
        jl.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                jl.setForeground(Color.BLUE);
                fireActionEvent(null);
            }
            public void mouseExited(MouseEvent evt) {
                jl.setForeground(Color.BLACK);
                fireActionEvent(null);
            }            
        });

      insertInBag(browsePanel, jl, 0, y++, 2, 1);
    }

//     if(runMode != null && runMode.equals(RunMode.Reviewer)) {
//       insertInBag(browsePanel,
//                   skipVdValidationCheckBox, 0, y, 2, 1);
//     }


    browsePanel.setPreferredSize(new Dimension(400, 250));

    this.add(browsePanel, BorderLayout.CENTER);

    browseButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          String xmiDir = UserPreferences.getInstance().getRecentDir();
          JFileChooser chooser = new JFileChooser(xmiDir);
          javax.swing.filechooser.FileFilter filter = 
            new javax.swing.filechooser.FileFilter() {
              public boolean accept(File f) {
                if (f.isDirectory()) {
                  return true;
                }                
                return f.getName().endsWith("." + fileExtension);
              }
              public String getDescription() {
                return fileExtension.toUpperCase() + " Files";
              }
            };
          
          chooser.setFileFilter(filter);
          int returnVal = chooser.showOpenDialog(null);
          if(returnVal == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            filePathField.setText(filePath);
            prefs.setRecentDir(filePath);
            fireActionEvent(evt);
          }
        }
      });

    progressPanel = new ProgressPanel(goal);
    
    if(isProgress) {
      progressPanel.setVisible(true);
      browseButton.setEnabled(false);
      filePathField.setEnabled(false);
    } else {
      progressPanel.setVisible(false);
    }

    this.add(progressPanel, BorderLayout.SOUTH);

  }

  private void insertInBag(JPanel bagComp, Component comp, int x, int y) {

    insertInBag(bagComp, comp, x, y, 1, 1);

  }

  private void insertInBag(JPanel bagComp, Component comp, int x, int y, int width, int height) {
    JPanel p = new JPanel();
    p.add(comp);

    bagComp.add(p, new GridBagConstraints(x, y, width, height, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  public void fireActionEvent(ActionEvent event) 
  {
    for(ActionListener l : actionListeners)
      l.actionPerformed(event);
  }
  
  public void addActionListener(ActionListener listener) 
  {
    actionListeners.add(listener);
  }

}