package gov.nih.nci.ncicb.cadsr.loader.ui;
import gov.nih.nci.ncicb.cadsr.domain.Concept;
import gov.nih.nci.ncicb.cadsr.domain.DataElement;
import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEvent;
import gov.nih.nci.ncicb.cadsr.loader.event.ReviewListener;
import gov.nih.nci.ncicb.cadsr.loader.ui.event.*;
import gov.nih.nci.ncicb.cadsr.loader.ui.tree.AttributeNode;
import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ClassNode;
import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ReviewableUMLNode;
import gov.nih.nci.ncicb.cadsr.loader.ui.tree.UMLNode;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.*;

public class ButtonPanel extends JPanel implements ActionListener, 
  PropertyChangeListener
{
  private JButton addButton, deleteButton, saveButton, deButton, ocButton;
  private JButton previousButton, nextButton;
  private JCheckBox reviewButton;
  
  private List<ReviewListener> reviewListeners 
    = new ArrayList<ReviewListener>();
    
  private List<NavigationListener> navigationListeners 
    = new ArrayList<NavigationListener>();
    
  private List<PropertyChangeListener> propChangeListeners 
    = new ArrayList<PropertyChangeListener>();
  
  private ConceptEditorPanel conceptEditorPanel;
  private UMLElementViewPanel viewPanel;
  private DEPanel dePanel;
  
  static final String ADD = "ADD",
    DELETE = "DELETE",
    SAVE = "APPLY", 
    PREVIOUS = "PREVIOUS",
    NEXT = "NEXT",
    AC = "AC",
    REVIEW = "REVIEW",
    SETUP = "SETUP",
    SWITCH = "SWITCH",
    OC = "OC",
    DELETEBUTTON = "DELETEBUTTON";
  
  public void addPropertyChangeListener(PropertyChangeListener l) {
    propChangeListeners.add(l);
  }
  
  public ButtonPanel(ConceptEditorPanel conceptEditorPanel, 
    UMLElementViewPanel viewPanel, DEPanel dePanel) 
  {
    this.conceptEditorPanel = conceptEditorPanel;
    this.viewPanel = viewPanel;
    this.dePanel = dePanel;
    
    addButton = new JButton("Add");
    deleteButton = new JButton("Remove");
    saveButton = new JButton("Apply");
    deButton = new JButton("Switch to DE");
    ocButton = new JButton("Switch to OC");
    reviewButton = new JCheckBox("<html>Human<br>Verified</html>");
    previousButton = new JButton("Previous");
    nextButton = new JButton("Next");
    
    reviewButton.setSelected(viewPanel.isReviewed());
    reviewButton.setActionCommand(REVIEW);
    addButton.setActionCommand(ADD);
    deleteButton.setActionCommand(DELETE);
    saveButton.setActionCommand(SAVE);
    previousButton.setActionCommand(PREVIOUS);
    nextButton.setActionCommand(NEXT);
    deButton.setActionCommand(AC);
    addButton.addActionListener(this);
    deleteButton.addActionListener(this);
    saveButton.addActionListener(this);
//    reviewButton.addItemListener(this);
    reviewButton.addActionListener(this);
    previousButton.addActionListener(this);
    nextButton.addActionListener(this);
    deButton.addActionListener(this);

    //JPanel buttonPanel = new JPanel();
    this.add(addButton);
    this.add(deleteButton);
    this.add(saveButton);
    this.add(reviewButton);
    this.add(previousButton);
    this.add(nextButton);
    this.add(deButton);
    this.add(ocButton);

    //this.add(buttonPanel, BorderLayout.SOUTH);
  }
  
  public void update() 
  {
    reviewButton.setSelected(viewPanel.isReviewed());
  }
  
  public void addReviewListener(ReviewListener listener) {
    reviewListeners.add(listener);
  }
  
  public void addNavigationListener(NavigationListener listener) 
  {
    navigationListeners.add(listener);
  }
  
  public void propertyChange(PropertyChangeEvent e) 
  {
    if(e.getPropertyName().equals(DELETE))
    {
      //Concept[] concepts = (Concept[])e.getNewValue();
      //if(concepts.length < 2)
        deleteButton.setEnabled((Boolean)e.getNewValue());      
    }
    
    else if(e.getPropertyName().equals(ADD)) 
    {
      addButton.setEnabled((Boolean)e.getNewValue());
    }
    
    else if(e.getPropertyName().equals(SAVE)) 
    {
      setSaveButtonState((Boolean)e.getNewValue());
      
        
    }   
    
    else if(e.getPropertyName().equals(REVIEW)) 
    {
      reviewButton.setEnabled((Boolean)e.getNewValue());
    }
    else if (e.getPropertyName().equals(SETUP)) 
    {
      initButtonPanel();
    }
    else if (e.getPropertyName().equals(SWITCH)) 
    {
      deButton.setEnabled((Boolean)e.getNewValue());
    }
  }
  
  private void fireNavigationEvent(NavigationEvent event) 
  {
    for(NavigationListener l : navigationListeners)
      l.navigate(event);
  }
  
  public void changeSwitchButton(String text) 
  {
    deButton.setText(text);
  }
  
  private void initButtonPanel() {  
    Concept[] concepts = conceptEditorPanel.getConcepts();
    if(concepts.length < 2)
      deleteButton.setEnabled(false);
    else
      deleteButton.setEnabled(true);
    
    if(conceptEditorPanel.areAllFieldEntered()) {
      reviewButton.setEnabled(true);
      addButton.setEnabled(true);
    } else {
      addButton.setEnabled(false);
      reviewButton.setEnabled(false);
    }
    setSaveButtonState(false);
    
    if(concepts.length == 0)
      reviewButton.setEnabled(false);
    
    if(conceptEditorPanel.getNode() instanceof AttributeNode)
      deButton.setVisible(true);
    else
      deButton.setVisible(false);
    
    if(conceptEditorPanel.getNode() instanceof ClassNode)
      ocButton.setVisible(true);
    else
      ocButton.setVisible(false);

//    JPanel buttonPanel = new JPanel();
//    buttonPanel.add(addButton);
//    buttonPanel.add(deleteButton);
//    buttonPanel.add(saveButton);
//    buttonPanel.add(reviewButton);
//    buttonPanel.add(previousButton);
//    buttonPanel.add(nextButton);
//    
//    this.add(buttonPanel, BorderLayout.SOUTH);
  }
  
  
  
  private void setSaveButtonState(boolean b) {
    saveButton.setEnabled(b);

    PropertyChangeEvent evt = new PropertyChangeEvent(this, SAVE, null, b);
    firePropertyChangeEvent(evt);
  }
  
  public void navigate(NavigationEvent evt) {  
    if(saveButton.isEnabled()) {
      if(JOptionPane.showConfirmDialog(this, "There are unsaved changes in this concept, would you like to apply the changes now?", "Unsaved Changes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) 
      {
        conceptEditorPanel.apply(false);
        dePanel.apply();
      }
    }
  }
  
  private void firePropertyChangeEvent(PropertyChangeEvent evt) {
    for(PropertyChangeListener l : propChangeListeners) 
      l.propertyChange(evt);
  }
  
    public void actionPerformed(ActionEvent evt) {
    AbstractButton button = (AbstractButton)evt.getSource();
    if(button.getActionCommand().equals(SAVE)) {
      conceptEditorPanel.applyPressed();
      dePanel.applyPressed();
      //updateHeaderLabels();
      //apply(false);
    } else if(button.getActionCommand().equals(ADD)) {
        conceptEditorPanel.addPressed();
 
    } else if(button.getActionCommand().equals(DELETE)) {
        conceptEditorPanel.removePressed();
    } else if(button.getActionCommand().equals(PREVIOUS)) {
      NavigationEvent event = new NavigationEvent(NavigationEvent.NAVIGATE_PREVIOUS);
      fireNavigationEvent(event);
      conceptEditorPanel.setRemove(false);
      //remove = false;
    } else if(button.getActionCommand().equals(NEXT)) {
      NavigationEvent event = new NavigationEvent(NavigationEvent.NAVIGATE_NEXT);
      fireNavigationEvent(event);
      conceptEditorPanel.setRemove(false);
      //remove = false;
    } else if(button.getActionCommand().equals(AC)) {
        if(deButton.getText().equals("Switch to DE")) {
        viewPanel.switchCards("DEPanel");
        deButton.setText("Switch to Concept");
        } else if (deButton.getText().equals("Switch to Concept")) {
      viewPanel.switchCards("Concept");
      deButton.setText("Switch to DE");
        } 
    } else if(button.getActionCommand().equals(REVIEW)) 
    {

      ReviewEvent event = new ReviewEvent();
      event.setUserObject(conceptEditorPanel.getNode()); 
      event.setReviewed(reviewButton.isSelected());

      fireReviewEvent(event);
      
      //if item is reviewed go to next item in the tree
      if(reviewButton.isSelected()) 
      {
        NavigationEvent goToNext = new NavigationEvent(NavigationEvent.NAVIGATE_NEXT);
        fireNavigationEvent(goToNext);
      }
        
    }
  }
 
  
  private void fireReviewEvent(ReviewEvent event) {
    for(ReviewListener l : reviewListeners)
      l.reviewChanged(event);
  }
  
  
}