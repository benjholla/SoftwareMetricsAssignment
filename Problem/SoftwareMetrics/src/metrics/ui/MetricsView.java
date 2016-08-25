package metrics.ui;


import java.io.File;
import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

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
	 * The main sequence base image
	 */
	private static final Image mainSequenceImage = ResourceManager.getPluginImage("metrics", "icons/main_sequence.png");

	/**
	 * The radius of the distance dot, note that this should be an even number
	 */
	private static final int DISPLAY_DOT_RAIDUS = 4;
	
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
		parent.setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite metricsComposite = new Composite(sashForm, SWT.BORDER);
		metricsComposite.setLayout(new GridLayout(2, false));
		
		final Label selectedPackageHeaderLabel = new Label(metricsComposite, SWT.NONE);
		selectedPackageHeaderLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		selectedPackageHeaderLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		selectedPackageHeaderLabel.setText("Selected Package:");
		
		final Label selectedPackageLabel = new Label(metricsComposite, SWT.NONE);
		selectedPackageLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		selectedPackageLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		selectedPackageLabel.setText("No package selected...");
		
		final Button showTypesButton = new Button(metricsComposite, SWT.NONE);
		showTypesButton.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		showTypesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		showTypesButton.setText("Show");
		
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
		
		final Label numTypesLabel = new Label(metricsComposite, SWT.NONE);
		numTypesLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		numTypesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		numTypesLabel.setToolTipText("Concrete classes, abstract classes, and interfaces");
		numTypesLabel.setText("# Types: 0");
		
		final Button showConcreteClassesButton = new Button(metricsComposite, SWT.NONE);
		showConcreteClassesButton.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		showConcreteClassesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		showConcreteClassesButton.setText("Show");
		
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
		
		final Label numConcreteClassesLabel = new Label(metricsComposite, SWT.NONE);
		numConcreteClassesLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		numConcreteClassesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		numConcreteClassesLabel.setToolTipText("Classes that are not abstract");
		numConcreteClassesLabel.setText("# Concrete Classes: 0");
		
		final Button showAbstractClassesButton = new Button(metricsComposite, SWT.NONE);
		showAbstractClassesButton.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		showAbstractClassesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		showAbstractClassesButton.setText("Show");
		
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
		
		final Label numAbstractClassesLabel = new Label(metricsComposite, SWT.NONE);
		numAbstractClassesLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		numAbstractClassesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		numAbstractClassesLabel.setToolTipText("Abstract classes and interfaces");
		numAbstractClassesLabel.setText("# Abstract Classes: 0");
		
		final Label abstractnessHeaderLabel = new Label(metricsComposite, SWT.NONE);
		abstractnessHeaderLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		abstractnessHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		abstractnessHeaderLabel.setToolTipText("The ratio of the number of abstract classes (and interfaces) "
				+ "in the package to the total number of classes in the analyzed package. The range for this "
				+ "metric is 0 to 1, with A=0 indicating a completely concrete package and A=1 indicating a "
				+ "completely abstract package.");
		abstractnessHeaderLabel.setText("Abstractness:");
		
		final Label abstractnessLabel = new Label(metricsComposite, SWT.NONE);
		abstractnessLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		abstractnessLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		abstractnessLabel.setText("Undefined");
		
		final Label afferentCouplingHeaderLabel = new Label(metricsComposite, SWT.NONE);
		afferentCouplingHeaderLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		afferentCouplingHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		afferentCouplingHeaderLabel.setToolTipText("Afferent Coupling (Ca): The number of classes outside the package that depend upon classes inside the package.");
		afferentCouplingHeaderLabel.setText("Afferent Coupling:");
		
		final Label afferentCouplingLabel = new Label(metricsComposite, SWT.NONE);
		afferentCouplingLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		afferentCouplingLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		afferentCouplingLabel.setText("Undefined");
		
		final Label efferentCouplingHeaderLabel = new Label(metricsComposite, SWT.NONE);
		efferentCouplingHeaderLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		efferentCouplingHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		efferentCouplingHeaderLabel.setToolTipText("Efferent Coupling (Ce): The number of classes outside the package that classes inside the package depend upon.");
		efferentCouplingHeaderLabel.setText("Efferent Coupling:");
		
		final Label efferentCouplingLabel = new Label(metricsComposite, SWT.NONE);
		efferentCouplingLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		efferentCouplingLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		efferentCouplingLabel.setText("Undefined");
		
		final Label instabilitiyHeaderLabel = new Label(metricsComposite, SWT.NONE);
		instabilitiyHeaderLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		instabilitiyHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		instabilitiyHeaderLabel.setToolTipText("Instability = Ce/(Ca+Ce).  Range [0,1] where 0 is very stable and 1 is very unstable.");
		instabilitiyHeaderLabel.setText("Instability:");
		
		final Label instabilitiyLabel = new Label(metricsComposite, SWT.NONE);
		instabilitiyLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		instabilitiyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		instabilitiyLabel.setText("Undefined");
		
		final Label distanceHeaderLabel = new Label(metricsComposite, SWT.NONE);
		distanceHeaderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		distanceHeaderLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		distanceHeaderLabel.setText("Distance:");
		
		final Label distanceLabel = new Label(metricsComposite, SWT.NONE);
		distanceLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		distanceLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 12, SWT.NORMAL));
		distanceLabel.setText("Undefined");
		
		final Composite idealSequenceComposite = new Composite(sashForm, SWT.BORDER);
		idealSequenceComposite.setLayout(new GridLayout(1, false));
		
		final Label idealSequenceLabel = new Label(idealSequenceComposite, SWT.NONE);
		idealSequenceLabel.setToolTipText("Double click to save image.");
		idealSequenceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
		idealSequenceLabel.setImage(mainSequenceImage);
		sashForm.setWeights(new int[] {1, 1});
		
		idealSequenceLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
			    dialog.setFilterNames(new String[] { "PNG Files", "All Files (*.*)" });
			    dialog.setFilterExtensions(new String[] { "*.png", "*.*" });
			    final String BASE_FILENAME = "abstractness_vs_instability.png";
			    if(packageSelection != null){
			    	dialog.setFileName(packageSelection.getAttr(XCSG.name).toString() + "_" + BASE_FILENAME);
			    } else {
			    	dialog.setFileName(BASE_FILENAME);
			    }
				String result = dialog.open();
				if(result != null || result.equals("")){
					File outputFile = new File(result);
					ImageLoader loader = new ImageLoader();
				    loader.data = new ImageData[] {idealSequenceLabel.getImage().getImageData()};
				    loader.save(outputFile.getAbsolutePath(), SWT.IMAGE_PNG);
				}
			}
		});
		
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
							  efferentCouplingLabel, instabilitiyLabel, distanceLabel, idealSequenceLabel);
			}				
		};
		
		// add the selection listener
		SelectionUtil.addSelectionListener(selectionListener);
	}
	
	// update the metrics display
	private void updateMetrics(final Label selectedPackageLabel, 
							   final Label numTypesLabel,
							   final Label numConcreteClassesLabel,
							   final Label numAbstractClassesLabel,
							   final Label abstractnessLabel,
							   final Label afferentCouplingLabel,
							   final Label efferentCouplingLabel,
							   final Label instabilitiyLabel,
							   final Label distanceLabel, 
							   final Label idealSequenceLabel){
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
			
			// calculate distance
			try {
				distanceLabel.setText(doubleToString(Metrics.getDistance(packageSelection)));
				
				// update the distance from main sequence overlay
				updateDistanceOverlay(idealSequenceLabel);
			} catch(ArithmeticException e1){
				distanceLabel.setText("Undefined");
			} catch (Exception e2){
				if(e2.getMessage().equals("Not Implemented!")){
					distanceLabel.setText("Not Implemented!");
				} else {
					distanceLabel.setText("ERROR");
					UIUtils.showError(e2, "An error calculating distance occured.");
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
			distanceLabel.setText("Undefined");
		}
	}

	private void updateDistanceOverlay(final Label idealSequenceLabel) {
		Image mainSequenceDistanceImage = new Image(Display.getCurrent(), mainSequenceImage.getImageData());
		
		double a = Metrics.getAbstractness(packageSelection);
		double i = Metrics.getInstability(packageSelection);
		
		int minX = 18;
		int maxX = 226;
		int minY = 24;
		int maxY = 230;
		
		int width = maxX - minX;
		int height = maxY - minY;
		
		int x = (int) (minX + ((double)width * i));
		int y = (int) (maxY - ((double)height * a));
		
		GC gc = new GC(mainSequenceDistanceImage);
		gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		gc.setAlpha(255);
		gc.fillOval(x, y, DISPLAY_DOT_RAIDUS*2, DISPLAY_DOT_RAIDUS*2);
		gc.dispose();
		idealSequenceLabel.setImage(mainSequenceDistanceImage);
		idealSequenceLabel.redraw();
	}

	@Override
	public void setFocus() {
		// intentionally left blank
	}
}
