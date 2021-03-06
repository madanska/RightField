package uk.ac.manchester.cs.owl.semspreadsheets.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import uk.ac.manchester.cs.owl.semspreadsheets.model.OntologyTermValidation;
import uk.ac.manchester.cs.owl.semspreadsheets.model.OntologyTermValidationDescriptor;
import uk.ac.manchester.cs.owl.semspreadsheets.model.OntologyTermValidationManager;
import uk.ac.manchester.cs.owl.semspreadsheets.model.Range;
import uk.ac.manchester.cs.owl.semspreadsheets.model.ValidationType;
import uk.ac.manchester.cs.owl.semspreadsheets.model.WorkbookManager;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 08-Nov-2009
 */
public class ClassHierarchyTree extends JTree {

    private WorkbookManager workbookManager;

    private boolean transmittingSelectioToModel;

    private boolean updatingSelectionFromModel;

    public ClassHierarchyTree(final WorkbookManager manager) {
        super(new ClassHierarchyTreeModel(manager));
        this.workbookManager = manager;
        manager.addListener(new WorkbookManagerListener() {
            public void workbookChanged(WorkbookManagerEvent event) {
            }

            public void workbookLoaded(WorkbookManagerEvent event) {
            }

            public void ontologiesChanged(WorkbookManagerEvent event) {
                setModel(new ClassHierarchyTreeModel(manager));
            }
        });
        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                transmitSelectionToModel();
            }
        });
        manager.getEntitySelectionModel().addListener(new EntitySelectionModelListener() {
            public void selectionChanged() {
                updateSelectionFromModel();
            }
        });
        setCellRenderer(new WorkbookManagerCellRenderer(workbookManager));
    }

    public ClassHierarchyTreeModel getClassHierarchyTreeModel() {
        return (ClassHierarchyTreeModel) super.getModel();
    }

    private void transmitSelectionToModel() {
        if(!updatingSelectionFromModel) {
            transmittingSelectioToModel = true;
            try {
                TreePath [] selectedPaths = getSelectionPaths();
                if(selectedPaths == null) {
                    return;
                }
                Set<OWLEntity> selectedEntities = new HashSet<OWLEntity>();
                for(TreePath path : selectedPaths) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if(node instanceof ClassHierarchyNode) {
                        selectedEntities.addAll(((ClassHierarchyNode) node).getOWLClasses());
                    }
                    else {
                        selectedEntities.add((OWLNamedIndividual) ((ClassHierarchyIndividualNode) node).getUserObject());
                    }
                }
                if(!selectedEntities.isEmpty()) {
                    OWLEntity selectedEntity = selectedEntities.iterator().next();
                    workbookManager.getEntitySelectionModel().setSelection(selectedEntity);
                    if (workbookManager.getLoadedOntologies().size() > 0) {
                        Range range = workbookManager.getSelectionModel().getSelectedRange();
                        OntologyTermValidationManager validationManager = workbookManager.getOntologyTermValidationManager();
                        Collection<OntologyTermValidation> validations = validationManager.getContainingValidations(range);
                        for(OntologyTermValidation validation : validations) {
                            if (!validation.getValidationDescriptor().getType().equals(ValidationType.NOVALIDATION)) {
                                if(!validation.getValidationDescriptor().getEntityIRI().equals(selectedEntity.getIRI())) {
                                    validationManager.removeValidation(validation);
                                    OntologyTermValidationDescriptor oldDescriptor = validation.getValidationDescriptor();
                                    OntologyTermValidationDescriptor newDescriptor = new OntologyTermValidationDescriptor(oldDescriptor.getType(), selectedEntity.getIRI(), workbookManager);
                                    validationManager.addValidation(new OntologyTermValidation(newDescriptor, validation.getRange()));
                                }
                            }
                        }
                    }
                }
                else {
                    workbookManager.getEntitySelectionModel().clearSelection();
                }
            }
            finally {
                transmittingSelectioToModel = false;
            }
        }
    }

    private void updateSelectionFromModel() {
        if(!transmittingSelectioToModel) {
            try {
                updatingSelectionFromModel = true;
                setSelectedClass((OWLClass) workbookManager.getEntitySelectionModel().getSelection());
            }
            finally {
                updatingSelectionFromModel = false;
            }
        }
    }

    public void setSelectedClass(OWLClass cls) {
        Collection<TreePath> treePaths = getClassHierarchyTreeModel().getTreePathsForEntity(cls);
        clearSelection();
        for(TreePath path : treePaths) {
            addSelectionPath(path);
            scrollPathToVisible(path);
        }

    }

    private Font ontologyNotLoadedFont = new Font("Lucida Grande", Font.BOLD, 14);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (workbookManager.getLoadedOntologies().size() == 0) {
            Color oldColor = g.getColor();
            g.setColor(Color.LIGHT_GRAY);
            Font oldFont = g.getFont();
            g.setFont(ontologyNotLoadedFont);
            String msg = "No ontologies loaded";
            Rectangle bounds = g.getFontMetrics().getStringBounds(msg, g).getBounds();
            g.drawString(msg, getWidth() / 2 - bounds.width / 2, getHeight() / 2 - g.getFontMetrics().getAscent());
            g.setFont(oldFont);
            g.setColor(oldColor);
        }
    }
}
