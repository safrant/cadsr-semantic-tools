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
package gov.nih.nci.ncicb.cadsr.loader.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * This class implements a basic wizard dialog, where the programmer can
 * insert one or more Components to act as panels. These panels can be navigated
 * through arbitrarily using the 'Next' or 'Back' buttons, or the dialog itself
 * can be closed using the 'Cancel' button. Note that even though the dialog
 * uses a CardLayout manager, the order of the panels is not linear. Each panel
 * determines at runtime what its next and previous panel will be.
 */
public class Wizard implements PropertyChangeListener {

    /**
     * Indicates that the 'Finish' button was pressed to close the dialog.
     */    
    public static final int FINISH_RETURN_CODE = 0;
    /**
     * Indicates that the 'Cancel' button was pressed to close the dialog.
     */    
    public static final int CANCEL_RETURN_CODE = 1;
    /**
     * Indicates that the dialog closed due to an internal error.
     */    
    public static final int ERROR_RETURN_CODE = 2;
        
    /**
     * The String-based action command for the 'Next' button.
     */    
    public static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
    /**
     * The String-based action command for the 'Back' button.
     */    
    public static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
    /**
     * The String-based action command for the 'Cancel' button.
     */    
    public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";
        
    /**
     * The default text used for the 'Back' button. Good candidate for i18n.
     */    
    public static final String DEFAULT_BACK_BUTTON_TEXT = "< Back";
    /**
     * The default text used for the 'Next' button. Good candidate for i18n.
     */    
    public static final String DEFAULT_NEXT_BUTTON_TEXT = "Next >";
    /**
     * The default text used for the 'Finish' button. Good candidate for i18n.
     */    
    public static final String DEFAULT_FINISH_BUTTON_TEXT = "Finish";
    /**
     * The default text used for the 'Cancel' button. Good candidate for i18n.
     */    
    public static final String DEFAULT_CANCEL_BUTTON_TEXT = "Cancel";
    
    private WizardModel wizardModel;
    private WizardController wizardController;
    private JDialog wizardDialog;
    
    private int returnCode;
    
    private JPanel cardPanel;
    private CardLayout cardLayout;
            
    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    /**
     * Default constructor. This method creates a new WizardModel object and passes it
     * into the overloaded constructor.
     */    
    public Wizard() {
        this(new WizardModel(), (Frame)null);
    }
    
    /**
     * This method creates a new WizardModel object and passes it into the overloaded
     * constructor. It accepts a java.awt.Dialog object as the javax.swing.JDialog's
     * parent.
     * @param owner The java.awt.Dialog object that is the owner of this dialog.
     */    
    public Wizard(Dialog owner) {
        this(new WizardModel(), owner);
    }
 
    /**
     * This method creates a new WizardModel object and passes it into the overloaded
     * constructor. It accepts a java.awt.Dialog object as the javax.swing.JDialog's
     * parent.
     * @param owner The java.awt.Frame object that is the owner of the javax.swing.JDialog.
     */    
    public Wizard(Frame owner) {
        this(new WizardModel(), owner);
    }
    
    
    /**
     * This constructor accepts a WizardModel object and a java.awt.Dialog object as the
     * wizard's JDialog's parent.
     * @param model The WizardModel object that serves as the model for the wizard dialog.
     * @param owner The java.awt.Dialog that is the owner of the generated javax.swing.JDialog.
     */    
    public Wizard(WizardModel model, Dialog owner) {
        
        wizardDialog = new JDialog(owner); 
        wizardModel = model;
        
        initComponents();
    }
    
    /**
     * This constructor accepts a WizardModel object and a java.awt.Frame object as the
     * wizard's javax.swing.JDialog's parent.
     * @param model The WizardModel object that serves as the model for this component.
     * @param owner The java.awt.Frame object that serves as the parent of the generated javax.swing.JDialog.
     */    
    public Wizard(WizardModel model, Frame owner) {
        
        wizardDialog = new JDialog(owner); 
        wizardModel = model;

        initComponents();
    }
    
    /**
     * Returns an instance of the JDialog that this class created. This is useful in
     * the event that you want to change any of the JDialog parameters manually.
     * @return The JDialog instance that this class created.
     */    
    public JDialog getDialog() {
        return wizardDialog;
    }
    
    /**
     * Returns the owner of the generated javax.swing.JDialog.
     * @return The owner (java.awt.Frame or java.awt.Dialog) of the javax.swing.JDialog generated
     * by this class.
     */    
    public Component getOwner() {
        return wizardDialog.getOwner();
    }
    
    /**
     * Sets the title of the generated javax.swing.JDialog.
     * @param s The title of the dialog.
     */    
    public void setTitle(String s) {
        wizardDialog.setTitle(s);
    }
    
    /**
     * Returns the current title of the generated dialog.
     * @return The String-based title of the generated dialog.
     */    
    public String getTitle() {
        return wizardDialog.getTitle();
    }
    
    /**
     * Sets the modality of the generated javax.swing.JDialog.
     * @param b the modality of the dialog
     */    
    public void setModal(boolean b) {
        wizardDialog.setModal(b);
    }
    
    /**
     * Returns the modality of the dialog.
     * @return A boolean indicating whether or not the generated javax.swing.JDialog is modal.
     */    
    public boolean isModal() {
        return wizardDialog.isModal();
    }
    
    /**
     * Convienenve method that displays a modal wizard dialog and blocks until the dialog
     * has completed.
     * @return Indicates how the dialog was closed. Compare this value against the RETURN_CODE
     * constants at the beginning of the class.
     */    
    public int showModalDialog() {
        
        wizardDialog.setModal(true);
        wizardDialog.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        wizardDialog.setLocation((screenSize.width - wizardDialog.getSize().width) / 2, (screenSize.height - wizardDialog.getSize().height) / 2);
        
        
        wizardDialog.show();
        
        return returnCode;
    }
    
    /**
     * Returns the current model of the wizard dialog.
     * @return A WizardModel instance, which serves as the model for the wizard dialog.
     */    
    public WizardModel getModel() {
        return wizardModel;
    }
    
    /**
     * Add a Component as a panel for the wizard dialog by registering its
     * WizardPanelDescriptor object. Each panel is identified by a unique Object-based
     * identifier (often a String), which can be used by the setCurrentPanel()
     * method to display the panel at runtime.
     * @param id An Object-based identifier used to identify the WizardPanelDescriptor object.
     * @param panel The WizardPanelDescriptor object which contains helpful information about the panel.
     */    
    public void registerWizardPanel(Object id, WizardPanelDescriptor panel) {
        
        //  Add the incoming panel to our JPanel display that is managed by
        //  the CardLayout layout manager.
        
        cardPanel.add(panel.getPanelComponent(), id);
        
        //  Set a callback to the current wizard.
        
        panel.setWizard(this);
        
        //  Place a reference to it in the model. 
        
        wizardModel.registerPanel(id, panel);
        
    }  
    
    /**
     * Displays the panel identified by the object passed in. This is the same Object-based
     * identified used when registering the panel.
     * @param id The Object-based identifier of the panel to be displayed.
     */    
    public void setCurrentPanel(Object id) {

        //  Get the hashtable reference to the panel that should
        //  be displayed. If the identifier passed in is null, then close
        //  the dialog.
        
        if (id == null)
            close(ERROR_RETURN_CODE);
        
        WizardPanelDescriptor oldPanelDescriptor = wizardModel.getCurrentPanelDescriptor();
        if (oldPanelDescriptor != null)
            oldPanelDescriptor.aboutToHidePanel();
        
        wizardModel.setCurrentPanel(id);
        wizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();
        
        //  Show the panel in the dialog.
        
        cardLayout.show(cardPanel, id.toString());
        wizardModel.getCurrentPanelDescriptor().displayingPanel();        
        
        
    }
    
    /**
     * Method used to listen for property change events from the model and update the
     * dialog's graphical components as necessary.
     * @param evt PropertyChangeEvent passed from the model to signal that one of its properties has changed value.
     */    
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (evt.getPropertyName().equals(WizardModel.CURRENT_PANEL_DESCRIPTOR_PROPERTY)) {
            wizardController.resetButtonsToPanelRules(); 
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_BUTTON_TEXT_PROPERTY)) {            
            nextButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_TEXT_PROPERTY)) {            
            backButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_TEXT_PROPERTY)) {            
            cancelButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_BUTTON_ENABLED_PROPERTY)) {            
            nextButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ENABLED_PROPERTY)) {            
            backButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ENABLED_PROPERTY)) {            
            cancelButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(WizardModel.NEXT_BUTTON_ICON_PROPERTY)) {            
            nextButton.setIcon((Icon)evt.getNewValue());
        } else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ICON_PROPERTY)) {            
            backButton.setIcon((Icon)evt.getNewValue());
        } else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ICON_PROPERTY)) {            
            cancelButton.setIcon((Icon)evt.getNewValue());
        }
        
    }
    
    /**
     * Retrieves the last return code set by the dialog.
     * @return An integer that identifies how the dialog was closed. See the *_RETURN_CODE
     * constants of this class for possible values.
     */    
    public int getReturnCode() {
        return returnCode;
    }

  public void doClickNext() {
    nextButton.doClick();
  }
  public void doClickBack() {
    backButton.doClick();
  }
    
    /**
     * Closes the dialog and sets the return code to the integer parameter.
     * @param code The return code.
     */    
    void close(int code) {
        returnCode = code;
        wizardDialog.dispose();
    }
    
    /**
     * This method initializes the components for the wizard dialog: it creates a JDialog
     * as a CardLayout panel surrounded by a small amount of space on each side, as well
     * as three buttons at the bottom.
     */
    
    private void initComponents() {

        wizardModel.addPropertyChangeListener(this);       
        wizardController = new WizardController(this);       

        wizardDialog.getContentPane().setLayout(new BorderLayout());
                
        //  Create the outer wizard panel, which is responsible for three buttons:
        //  Next, Back, and Cancel. It is also responsible a JPanel above them that
        //  uses a CardLayout layout manager to display multiple panels in the 
        //  same spot.
        
        JPanel buttonPanel = new JPanel();
        JSeparator separator = new JSeparator();
        Box buttonBox = new Box(BoxLayout.LINE_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       

        cardLayout = new CardLayout(); 
        cardPanel.setLayout(cardLayout);
        
        backButton = new JButton();
        nextButton = new JButton();
        cancelButton = new JButton();

        backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
        nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);

        backButton.addActionListener(wizardController);
        nextButton.addActionListener(wizardController);
        cancelButton.addActionListener(wizardController);
        
        KeyListener keyListener = new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
              if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
                ((JButton)evt.getComponent()).doClick();
              }
            }
          };
        backButton.addKeyListener(keyListener);
        nextButton.addKeyListener(keyListener);
        cancelButton.addKeyListener(keyListener);

        //  Create the buttons with a separator above them, then place them
        //  on the east side of the panel with a small amount of space between
        //  the back and the next button, and a larger amount of space between
        //  the next button and the cancel button.
        
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(separator, BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));       
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);

        if (wizardModel.getBackButtonText() == null)
            wizardModel.setBackButtonText(Wizard.DEFAULT_BACK_BUTTON_TEXT);
        if (wizardModel.getNextButtonText() == null)
            wizardModel.setNextButtonText(Wizard.DEFAULT_NEXT_BUTTON_TEXT);
        if (wizardModel.getCancelButtonText() == null)
            wizardModel.setCancelButtonText(Wizard.DEFAULT_CANCEL_BUTTON_TEXT);
        
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);

        wizardDialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        wizardDialog.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);

    }

}
