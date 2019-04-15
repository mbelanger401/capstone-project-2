/* Author monica*/
package edu.njit.knime.adapter.nodes.qvx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import org.knime.base.node.io.filereader.ColProperty;
import org.knime.base.node.io.filereader.FileAnalyzer;
import org.knime.base.node.io.filereader.FileReaderExecutionMonitor;
import org.knime.base.node.io.filereader.FileReaderNodeSettings;
import org.knime.base.node.io.filereader.FileTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.workflow.NodeProgress;
import org.knime.core.node.workflow.NodeProgressEvent;
import org.knime.core.node.workflow.NodeProgressListener;
import org.knime.core.util.FileUtil;
import org.knime.core.util.tokenizer.SettingsStatus;

import edu.njit.knime.adapter.qvx.QVXReader;
import edu.njit.knime.adapter.qvx.QvxTableHeader;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of Qvx.
 * Qvx Node
 *
 * @author Monica
 */
public class QvxNodeModel extends NodeModel {
    
    private static final NodeLogger logger = NodeLogger.getLogger(QvxNodeModel.class);
    
	public static final String DEFAULT_PATH = "./";

	public static final String CFGKEY_FILE_PATH = "FilePath";

    private final SettingsModelString filepath = new SettingsModelString(CFGKEY_FILE_PATH, DEFAULT_PATH);
                
    private Vector<ColProperty> columnProperties;

    private int numOfColumns;


    protected QvxNodeModel() {
            super(0, 1);
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        QvxFileTable fTable = createFileTable(exec);
        try {
            BufferedDataTable table = exec.createBufferedDataTable(fTable, exec.createSubExecutionContext(0.0));
            return new BufferedDataTable[] {table};
        } finally {
            fTable.dispose();
        }
    }

    protected QvxFileTable createFileTable(final ExecutionContext exec) throws Exception {
        QvxFileReaderNodeSettings settings = new QvxFileReaderNodeSettings();

        CheckUtils.checkSourceFile(filepath.getStringValue());
        URL url = FileUtil.toURL(filepath.getStringValue());

        settings.setDataFileLocationAndUpdateTableName(url);
        settings.setQvxReader(new QVXReader(filepath.getStringValue()));
        settings.setQvxTableHeader(settings.getQvxReader().getTableHeader());

        numOfColumns = settings.getQvxReader().getNumColumns();
        
        columnProperties = settings.getQvxReader().getColumnProperties();

        
        final ExecutionMonitor analyseExec = exec.createSubProgress(0.5);
        final ExecutionContext readExec = exec.createSubExecutionContext(0.5);
        exec.setMessage("Analyzing file");

        final DataTableSpec tableSpec = createDataTableSpec();
        if (tableSpec == null) {

                throw new IllegalStateException("Unknown error during file analysis.");

        }
        exec.setMessage("Buffering file");
        return new QvxFileTable(tableSpec, settings, readExec);
    }

    


	public DataTableSpec createDataTableSpec() {

        Vector<DataColumnSpec> cSpec = new Vector<DataColumnSpec>();
        for (int c = 0; c < numOfColumns; c++) {
            ColProperty cProp = columnProperties.get(c);
            if (!cProp.getSkipThisColumn()) {
                cSpec.add(cProp.getColumnSpec());
            }
        }

        return new DataTableSpec(filepath.getStringValue(),
                cSpec.toArray(new DataColumnSpec[cSpec.size()]));
    }

    @Override
    protected void reset() {
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{null};
    }


    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        filepath.saveSettingsTo(settings);

    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        filepath.loadSettingsFrom(settings);

    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        filepath.validateSettings(settings);

    }
    
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }
    
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}
