/*******************************************************************************
 * Copyright (c) 2013, 2017 Jan Holy
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jan Holy - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.ui.editors;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.core.PcaUtils;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.IPcaResult;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.IPcaResults;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.ISample;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.ISampleData;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.ui.untility.PcaColorGroup;
import org.eclipse.chemclipse.model.core.IChromatogramOverview;
import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.model.core.IPeaks;
import org.eclipse.chemclipse.model.targets.IPeakTarget;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class SamplesOverviewPage {

	private Combo comboSelectData;
	private Label countSelectedSamples;
	private HashMap<ISample, String> groupNames = new HashMap<>();
	private Composite mainComposite;
	private Map<String, Color> mapGroupColor = new HashMap<>();
	private NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	final private String[] overviewType = new String[]{"PCA", "Data", "Normlized data", "Peaks"};
	private int overviewTypeSelection = -1;
	private PcaEditor pcaEditor;
	private IPcaResults pcaResults;
	private ISample selectedSample;
	private Label selectedSampleLable;
	private Table tableOverview;
	private CheckboxTableViewer tableSamples;

	public SamplesOverviewPage(PcaEditor pcaEditor, TabFolder tabFolder, FormToolkit formToolkit) {
		//
		this.pcaEditor = pcaEditor;
		initialize(tabFolder);
	}

	private void createColumnsTableOverview(String[] columnsName) {

		tableOverview.setRedraw(false);
		for(TableColumn column : tableOverview.getColumns()) {
			column.dispose();
		}
		for(String name : columnsName) {
			TableColumn tableColumn = new TableColumn(tableOverview, SWT.None);
			tableColumn.setText(name);
		}
		tableOverview.setRedraw(true);
	}

	private void createColumnsTableSamples() {

		String[] titles = {"Filename", "Group"};
		int[] bounds = {100, 100};
		// first column is for the first name
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				ISample sample = (ISample)element;
				return sample.getName();
			}
		});
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {

				ISample sample = (ISample)cell.getElement();
				String groupName = groupNames.get(sample);
				cell.setImage(getGroupColorImage(groupName));
				cell.setText(groupName != null ? groupName : "");
			}
		});
		col.setEditingSupport(new EditingSupport(tableSamples) {

			private TextCellEditor editor = new TextCellEditor(tableSamples.getTable());

			@Override
			protected boolean canEdit(Object element) {

				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {

				return editor;
			}

			@Override
			protected Object getValue(Object element) {

				ISample sample = (ISample)element;
				String groupName = groupNames.get(sample);
				if(groupName == null) {
					return "";
				} else {
					return groupName;
				}
			}

			@Override
			protected void setValue(Object element, Object value) {

				ISample sample = (ISample)element;
				String groupName = (String)value;
				groupName = groupName.trim();
				groupName = groupName.isEmpty() ? null : groupName;
				groupNames.put(sample, groupName);
				updateTableTableSamples();
			}
		});
	}

	/**
	 * Creates the file count labels.
	 *
	 * @param client
	 */
	private void createLabelSamplesSelection(Composite client) {

		countSelectedSamples = new Label(client, SWT.NONE);
		countSelectedSamples.setText("Selected: 0 from 0 samples.");
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		countSelectedSamples.setLayoutData(gridData);
	}

	private void createLableSelectedSample(Composite client) {

		selectedSampleLable = new Label(client, SWT.NONE);
		selectedSampleLable.setText("Selected Sample:");
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		countSelectedSamples.setLayoutData(gridData);
	}

	private void createOverviewSamples(Composite parent) {

		Composite client = new Composite(parent, SWT.None);
		GridData gridData = new GridData(GridData.FILL_VERTICAL);
		gridData.widthHint = 400;
		client.setLayoutData(gridData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		client.setLayout(layout);
		/*
		 * Creates the table and the action buttons.
		 */
		createTableSamples(client);
		createLabelSamplesSelection(client);
		client.pack();
	}

	private void createOverviewSection(Composite parent) {

		Composite composite = new Composite(parent, SWT.None);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, false));
		comboSelectData = new Combo(composite, SWT.READ_ONLY);
		for(String name : overviewType) {
			comboSelectData.add(name);
		}
		comboSelectData.addListener(SWT.Selection, e -> {
			overviewTypeSelection = comboSelectData.getSelectionIndex();
			selectDataTableOverview(true);
		});
		comboSelectData.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tableOverview = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		tableOverview.setHeaderVisible(true);
		tableOverview.setLinesVisible(true);
		tableOverview.setLayoutData(new GridData(GridData.FILL_BOTH));
		createLableSelectedSample(composite);
	}

	private void createTableSamples(Composite client) {

		GridData gridData;
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 400;
		gridData.widthHint = 100;
		gridData.verticalSpan = 5;
		Table table = new Table(client, SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(gridData);
		tableSamples = new CheckboxTableViewer(table);
		tableSamples.setContentProvider(new ArrayContentProvider());
		tableSamples.addSelectionChangedListener(event -> {
			IStructuredSelection selection = (IStructuredSelection)event.getSelection();
			if(!selection.isEmpty()) {
				ISample sample = ((ISample)selection.getFirstElement());
				if(sample != null && sample != selectedSample) {
					selectedSample = sample;
					selectDataTableOverview(false);
					selectedSampleLable.setText("Selected Sample: " + selectedSample.getName());
					selectedSampleLable.getParent().layout();
				}
			}
		});
		createColumnsTableSamples();
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {

		final TableViewerColumn viewerColumn = new TableViewerColumn(tableSamples, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	private Image getGroupColorImage(String groupName) {

		Color color = mapGroupColor.get(groupName);
		int len = 16;
		Image image = new Image(Display.getCurrent(), len, len);
		GC gc = new GC(image);
		gc.setBackground(color);
		gc.fillRectangle(0, 0, len, len);
		gc.dispose();
		return image;
	}

	private void initialize(TabFolder tabFolder) {

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Samples Overview");
		mainComposite = new Composite(tabFolder, SWT.None);
		mainComposite.setLayout(new GridLayout(3, false));
		/*
		 * Create the section.
		 */
		createOverviewSamples(mainComposite);
		/*
		 * create sample overview
		 */
		createOverviewSection(mainComposite);
		/*
		 * create button area
		 */
		Composite buttonComposite = new Composite(mainComposite, SWT.None);
		buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, true));
		buttonComposite.setLayout(new FillLayout(SWT.VERTICAL));
		Button button = new Button(buttonComposite, SWT.PUSH);
		button.setText("Select All");
		button.addListener(SWT.Selection, e -> tableSamples.setAllChecked(true));
		button = new Button(buttonComposite, SWT.PUSH);
		button.setText("Deselect All");
		button.addListener(SWT.Selection, e -> tableSamples.setAllChecked(false));
		button = new Button(buttonComposite, SWT.PUSH);
		button.setText("Update");
		button.addListener(SWT.Selection, e -> updateSamples());
		button = new Button(buttonComposite, SWT.PUSH);
		button.setText("Reset");
		button.addListener(SWT.Selection, e -> resetSamples());
		setEnable(mainComposite, false);
		tabItem.setControl(mainComposite);
	}

	private void redrawSamplesSelectedCount() {

		long selected = pcaResults.getSampleList().stream().filter(ISample::isSelected).count();
		countSelectedSamples.setText("Selected: " + selected + " from " + pcaResults.getSampleList().size() + " samples");
	}

	private void resetSamples() {

		pcaResults.getSampleList().forEach(s -> {
			tableSamples.setChecked(s, s.isSelected());
			groupNames.put(s, s.getGroupName());
		});
		updateTableTableSamples();
	}

	private void selectDataTableOverview(boolean updateColumns) {

		switch(overviewTypeSelection) {
			case 0:
				setColumnsPCAOverview(updateColumns);
				break;
			case 1:
				setColumnsRawDataOverview(updateColumns);
				break;
			case 2:
				setColumnsNormalizetedDataOverview(updateColumns);
				break;
			case 3:
				setColumnsPeaksOverview(updateColumns);
				break;
		}
		updateOverviewTable();
	}

	private void setColumnsNormalizetedDataOverview(boolean updateColumns) {

		tableOverview.clearAll();
		tableOverview.removeAll();
		if(updateColumns) {
			createColumnsTableOverview(new String[]{"Retention Time (Mintes)", "Data"});
		}
		if(selectedSample != null) {
			List<ISampleData> sampleData = selectedSample.getSampleData();
			for(int i = 0; i < sampleData.size(); i++) {
				TableItem tableItem = new TableItem(tableOverview, SWT.NONE);
				tableItem.setText(0, nf.format(pcaResults.getExtractedRetentionTimes().get(i) / IChromatogramOverview.MINUTE_CORRELATION_FACTOR));
				tableItem.setText(1, Double.toString(sampleData.get(i).getNormalizedData()));
			}
		}
	}

	private void setColumnsPCAOverview(boolean updateColumns) {

		tableOverview.clearAll();
		tableOverview.removeAll();
		if(updateColumns) {
			createColumnsTableOverview(new String[]{"", ""});
		}
		if(selectedSample != null) {
			IPcaResult pcaResult = selectedSample.getPcaResult();
			double[] eigenSapace = pcaResult.getEigenSpace();
			for(int i = 0; i < eigenSapace.length; i++) {
				TableItem tableItem = new TableItem(tableOverview, SWT.NONE);
				tableItem.setText(0, "PC " + (i + 1));
				tableItem.setText(1, Double.toString(eigenSapace[i]));
			}
		}
	}

	private void setColumnsPeaksOverview(boolean updateColumns) {

		tableOverview.clearAll();
		tableOverview.removeAll();
		if(updateColumns) {
			createColumnsTableOverview(new String[]{"Reten.time at Max (Minutes)", "Peak Integrator Area", "Peaks Name"});
		}
		if(selectedSample != null) {
			IPeaks peaks = selectedSample.getPcaResult().getPeaks();
			if(peaks != null) {
				Map<Integer, IPeak> sortedPeaks = new TreeMap<>();
				peaks.getPeaks().forEach(p -> sortedPeaks.put(p.getPeakModel().getRetentionTimeAtPeakMaximum(), p));
				Iterator<Entry<Integer, IPeak>> it = sortedPeaks.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<Integer, IPeak> entry = it.next();
					int retentionTime = entry.getKey();
					IPeak peak = entry.getValue();
					TableItem tableItem = new TableItem(tableOverview, SWT.NONE);
					tableItem.setText(0, Integer.toString(retentionTime));
					tableItem.setText(1, Double.toString(peak.getIntegratedArea()));
					List<IPeakTarget> targets = peak.getTargets();
					if(!targets.isEmpty()) {
						tableItem.setText(2, targets.get(0).getLibraryInformation().getName());
					}
				}
			}
		}
	}

	private void setColumnsRawDataOverview(boolean updateColumns) {

		tableOverview.clearAll();
		tableOverview.removeAll();
		if(updateColumns) {
			createColumnsTableOverview(new String[]{"Retention Time (Mintes)", "Data"});
		}
		if(selectedSample != null) {
			List<ISampleData> sampleData = selectedSample.getSampleData();
			for(int i = 0; i < sampleData.size(); i++) {
				TableItem tableItem = new TableItem(tableOverview, SWT.NONE);
				tableItem.setText(0, nf.format(pcaResults.getExtractedRetentionTimes().get(i) / IChromatogramOverview.MINUTE_CORRELATION_FACTOR));
				tableItem.setText(1, Double.toString(sampleData.get(i).getData()));
			}
		}
	}

	private void setEnable(Composite parent, boolean enable) {

		for(Control control : parent.getChildren()) {
			if(control instanceof Composite) {
				Composite composite = (Composite)control;
				setEnable(composite, enable);
			}
			control.setEnabled(enable);
		}
	}

	public void update() {

		Optional<IPcaResults> pcaResults = pcaEditor.getPcaResults();
		if(pcaResults.isPresent()) {
			this.pcaResults = pcaResults.get();
			List<ISample> samples = this.pcaResults.getSampleList();
			groupNames.clear();
			samples.forEach(s -> groupNames.put(s, s.getGroupName()));
			PcaUtils.sortSampleListByName(samples);
			updateColorMap();
			tableSamples.setInput(samples);
			updateTableTableSamples();
			pcaResults.get().getSampleList().forEach(s -> tableSamples.setChecked(s, s.isSelected()));
			redrawSamplesSelectedCount();
			selectDataTableOverview(false);
			tableSamples.getTable().select(0);
			tableSamples.getTable().notifyListeners(SWT.Selection, new Event());
			comboSelectData.select(0);
			comboSelectData.notifyListeners(SWT.Selection, new Event());
			setEnable(mainComposite, true);
		} else {
			setEnable(mainComposite, false);
		}
	}

	private void updateColorMap() {

		mapGroupColor = PcaColorGroup.getColorSWT(groupNames.values().stream().collect(Collectors.toSet()));
	}

	private void updateOverviewTable() {

		for(TableColumn column : tableOverview.getColumns()) {
			column.pack();
		}
		tableOverview.redraw();
	}

	private void updateSamples() {

		pcaResults.getSampleList().forEach(s -> {
			s.setGroupName(groupNames.get(s));
			s.setSelected(false);
		});
		Object[] checkedSamples = tableSamples.getCheckedElements();
		for(Object checkedSample : checkedSamples) {
			((ISample)checkedSample).setSelected(true);
		}
		pcaEditor.updataSamples();
	}

	private void updateTableTableSamples() {

		updateColorMap();
		for(TableColumn column : tableSamples.getTable().getColumns()) {
			column.pack();
		}
		tableSamples.refresh();
	}
}
