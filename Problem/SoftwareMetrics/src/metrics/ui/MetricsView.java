package metrics.ui;


import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

import metrics.Metrics;


public class MetricsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "metrics.ui.MetricsView";

	/**
	 * The constructor.
	 */
	public MetricsView() {
		// intentionally left blank
	}
	
	// the current Atlas package selection
	private Node packageSelection = null;
	
	private String doubleToString(double d){
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(d);
	}
	
	private String getPackageSelectionName(){
		if(packageSelection != null){
			return packageSelection.attr().get(XCSG.name).toString();
		} else {
			return "No package selected...";
		}
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		
		final Label selectedPackageHeaderLabel = new Label(parent, SWT.NONE);
		selectedPackageHeaderLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		selectedPackageHeaderLabel.setText("Selected Package:");
		
		final Label selectedPackageLabel = new Label(parent, SWT.NONE);
		selectedPackageLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		selectedPackageLabel.setText("No package selected...");
		
		final Button showTypesButton = new Button(parent, SWT.NONE);
		showTypesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		showTypesButton.setText("Show");
		
		final Label numTypesLabel = new Label(parent, SWT.NONE);
		numTypesLabel.setToolTipText("Concrete classes, abstract classes, and interfaces");
		numTypesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		numTypesLabel.setText("# Types: 0");
		
		final Button showConcreteClassesButton = new Button(parent, SWT.NONE);
		showConcreteClassesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		showConcreteClassesButton.setText("Show");
		
		final Label numConcreteClassesLabel = new Label(parent, SWT.NONE);
		numConcreteClassesLabel.setToolTipText("Classes that are not abstract");
		numConcreteClassesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		numConcreteClassesLabel.setText("# Concrete Classes: 0");
		
		final Button showAbstractClassesButton = new Button(parent, SWT.NONE);
		showAbstractClassesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		showAbstractClassesButton.setText("Show");
		
		final Label numAbstractClassesLabel = new Label(parent, SWT.NONE);
		numAbstractClassesLabel.setToolTipText("Abstract classes and interfaces");
		numAbstractClassesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		numAbstractClassesLabel.setText("# Abstract Classes: 0");
		
		final Label abstractnessHeaderLabel = new Label(parent, SWT.NONE);
		abstractnessHeaderLabel.setToolTipText("The ratio of the number of abstract classes (and interfaces) "
				+ "in the package to the total number of classes in the analyzed package. The range for this "
				+ "metric is 0 to 1, with A=0 indicating a completely concrete package and A=1 indicating a "
				+ "completely abstract package.");
		abstractnessHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		abstractnessHeaderLabel.setText("Abstractness:");
		
		final Label abstractnessLabel = new Label(parent, SWT.NONE);
		abstractnessLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		abstractnessLabel.setText("Undefined");
		
		final Label afferentCouplingHeaderLabel = new Label(parent, SWT.NONE);
		afferentCouplingHeaderLabel.setToolTipText("Afferent Coupling (Ca): The number of classes outside the package that depend upon classes inside the package.");
		afferentCouplingHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		afferentCouplingHeaderLabel.setText("Afferent Coupling:");
		
		final Label afferentCouplingLabel = new Label(parent, SWT.NONE);
		afferentCouplingLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		afferentCouplingLabel.setText("Undefined");
		
		final Label efferentCouplingHeaderLabel = new Label(parent, SWT.NONE);
		efferentCouplingHeaderLabel.setToolTipText("Efferent Coupling (Ce): The number of classes outside the package that classes inside the package depend upon.");
		efferentCouplingHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		efferentCouplingHeaderLabel.setText("Efferent Coupling:");
		
		final Label efferentCouplingLabel = new Label(parent, SWT.NONE);
		efferentCouplingLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		efferentCouplingLabel.setText("Undefined");
		
		final Label instabilitiyHeaderLabel = new Label(parent, SWT.NONE);
		instabilitiyHeaderLabel.setToolTipText("Instability = Ce/(Ca+Ce).  Range [0,1] where 0 is very stable and 1 is very unstable.");
		instabilitiyHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		instabilitiyHeaderLabel.setText("Instability:");
		
		final Label instabilitiyLabel = new Label(parent, SWT.NONE);
		instabilitiyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		instabilitiyLabel.setText("Undefined");
		
		// setup the Atlas selection event listener
		IAtlasSelectionListener selectionListener = new IAtlasSelectionListener(){
			@Override
			public void selectionChanged(IAtlasSelectionEvent atlasSelection) {
				try {
					// grab the first package node out of the set if there are multiple selections
					AtlasSet<Node> selectedPackages = Metrics.getPackages(atlasSelection.getSelection()).eval().nodes();
					if(selectedPackages.size() > 0){
						packageSelection = selectedPackages.getFirst();
					} else {
						packageSelection = null;
					}
				} catch (Exception e){
					packageSelection = null;
				}
				updateMetrics(selectedPackageLabel, numTypesLabel, numConcreteClassesLabel, 
							  numAbstractClassesLabel, abstractnessLabel, afferentCouplingLabel, 
							  efferentCouplingLabel, instabilitiyLabel);
			}				
		};
		
		// add the selection listener
		SelectionUtil.addSelectionListener(selectionListener);
		
		// add the show button events
		showTypesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(packageSelection != null){
					try {
						Q packageTypes = Metrics.getPackageTypes(packageSelection);
						UIUtils.show(packageTypes, null, true, getPackageSelectionName() + " Types");
					} catch (Exception ex){
						UIUtils.showError(ex, "An error finding package types occured.");
					}
				} else {
					UIUtils.showMessage("Alert", "Please select a package!");
				}
			}
		});
		
		showConcreteClassesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(packageSelection != null){
					try {
						Q packageTypes = Metrics.getPackageTypes(packageSelection);
						Q concretePackageClasses = Metrics.getConcreteClasses(packageTypes);
						UIUtils.show(concretePackageClasses, null, true, getPackageSelectionName() + " Concrete Classes");
					} catch (Exception ex){
						UIUtils.showError(ex, "An error finding concrete package classes occured.");
					}
				} else {
					UIUtils.showMessage("Alert", "Please select a package!");
				}
			}
		});
		
		showAbstractClassesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(packageSelection != null){
					try {
						Q packageTypes = Metrics.getPackageTypes(packageSelection);
						Q abstractPackageClasses = Metrics.getAbstractClasses(packageTypes);
						UIUtils.show(abstractPackageClasses, null, true, getPackageSelectionName() + " Abstract Classes");
					} catch (Exception ex){
						UIUtils.showError(ex, "An error finding abstract package classes occured.");
					}
				} else {
					UIUtils.showMessage("Alert", "Please select a package!");
				}
			}
		});
	}
	
	// update the metrics display
	private void updateMetrics(final Label selectedPackageLabel, 
							   final Label numTypesLabel,
							   final Label numConcreteClassesLabel,
							   final Label numAbstractClassesLabel,
							   final Label abstractnessLabel,
							   final Label afferentCouplingLabel,
							   final Label efferentCouplingLabel,
							   final Label instabilitiyLabel){
		// if the selected package is valid then update display, otherwise set values to undefined
		if(packageSelection != null){
			// set the package display name
			selectedPackageLabel.setText(getPackageSelectionName());
			
			// count types
			try {
				Q packageTypes = Metrics.getPackageTypes(packageSelection);
				numTypesLabel.setText("# Types: " + Metrics.countNodes(packageTypes));
			} catch (Exception e){
				if(e.getMessage().equals("Not Implemented!")){
					numTypesLabel.setText("Not Implemented!");
				} else {
					numTypesLabel.setText("ERROR");
					UIUtils.showError(e, "An error counting types occured.");
				}
			}
			
			// count concrete classes
			try {
				Q packageTypes = Metrics.getPackageTypes(packageSelection);
				Q concretePackageClasses = Metrics.getConcreteClasses(packageTypes);
				numConcreteClassesLabel.setText("# Concrete Classes: " + Metrics.countNodes(concretePackageClasses));
			} catch (Exception e){
				if(e.getMessage().equals("Not Implemented!")){
					numConcreteClassesLabel.setText("Not Implemented!");
				} else {
					numConcreteClassesLabel.setText("ERROR");
					UIUtils.showError(e, "An error counting concrete classes occured.");
				}
			}
			
			// count abstract classes
			try {
				Q packageTypes = Metrics.getPackageTypes(packageSelection);
				Q abstractPackageClasses = Metrics.getAbstractClasses(packageTypes);
				numAbstractClassesLabel.setText("# Abstract Classes: " + Metrics.countNodes(abstractPackageClasses));
			} catch (Exception e){
				if(e.getMessage().equals("Not Implemented!")){
					numAbstractClassesLabel.setText("Not Implemented!");
				} else {
					numAbstractClassesLabel.setText("ERROR");
					UIUtils.showError(e, "An error counting abstract classes occured.");
				}
			}
			
			// calculate abstractness
			try {
				abstractnessLabel.setText(doubleToString(Metrics.getAbstractness(packageSelection)));
			} catch(ArithmeticException e1){
				abstractnessLabel.setText("Undefined");
			} catch (Exception e2){
				if(e2.getMessage().equals("Not Implemented!")){
					abstractnessLabel.setText("Not Implemented!");
				} else {
					abstractnessLabel.setText("ERROR");
					UIUtils.showError(e2, "An error calculating abstractness occured.");
				}
			}

			// calculate afferent coupling
			try {
				afferentCouplingLabel.setText("" + Metrics.countNodes(Metrics.getAfferentCouplings(packageSelection)));
			} catch (Exception e){
				if(e.getMessage().equals("Not Implemented!")){
					afferentCouplingLabel.setText("Not Implemented!");
				} else {
					afferentCouplingLabel.setText("ERROR");
					UIUtils.showError(e, "An error calculating afferent coupling occured.");
				}
			}
			
			// calculate efferent coupling
			try {
				efferentCouplingLabel.setText("" + Metrics.countNodes(Metrics.getEfferentCouplings(packageSelection)));
			} catch (Exception e){
				if(e.getMessage().equals("Not Implemented!")){
					efferentCouplingLabel.setText("Not Implemented!");
				} else {
					efferentCouplingLabel.setText("ERROR");
					UIUtils.showError(e, "An error calculating efferent coupling occured.");
				}
			}
			
			// calculate instability
			try {
				instabilitiyLabel.setText(doubleToString(Metrics.getInstability(packageSelection)));
			} catch(ArithmeticException e1){
				instabilitiyLabel.setText("Undefined");
			} catch (Exception e2){
				if(e2.getMessage().equals("Not Implemented!")){
					instabilitiyLabel.setText("Not Implemented!");
				} else {
					instabilitiyLabel.setText("ERROR");
					UIUtils.showError(e2, "An error calculating instability occured.");
				}
			}
		} else {
			selectedPackageLabel.setText("No package selected...");
			numTypesLabel.setText("# Types: 0");
			numConcreteClassesLabel.setText("# Concrete Classes: 0");
			numAbstractClassesLabel.setText("# Abstract Classes: 0");
			abstractnessLabel.setText("Undefined");
			afferentCouplingLabel.setText("Undefined");
			efferentCouplingLabel.setText("Undefined");
			instabilitiyLabel.setText("Undefined");
		}
	}

	@Override
	public void setFocus() {
		// intentionally left blank
	}
}
