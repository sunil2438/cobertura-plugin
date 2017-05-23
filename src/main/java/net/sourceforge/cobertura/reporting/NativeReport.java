package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.util.FileFinder;

import java.io.File;

public class NativeReport implements Report {
	private NullReport nullReport;
	private ProjectData projectData;
	private File destinationDir;
	private FileFinder finder;
	private ComplexityCalculator complexity;
	private String encoding;
	private ReportFormatStrategyRegistry formatStrategyRegistry;

	public NativeReport(ProjectData projectData, File destinationDir,
			FileFinder finder, ComplexityCalculator complexity, String encoding) {
		this.nullReport = new NullReport();
		this.projectData = projectData;
		this.destinationDir = destinationDir;
		this.finder = finder;
		this.complexity = complexity;
		this.destinationDir = destinationDir;
		this.encoding = encoding;
		formatStrategyRegistry = ReportFormatStrategyRegistry.getInstance();
	}

	public void export(ReportFormat reportFormat) {
		formatStrategyRegistry.getReportFormatStrategy(reportFormat).save(this);
	}

	public ReportName getName() {
		return ReportName.COVERAGE_REPORT;
	}

	public Report getByName(ReportName name) {
		if (getName().equals(name)) {
			return this;
		}
		return nullReport;
	}

	public ProjectData getProjectData() {
		return projectData;
	}

	public File getDestinationDir() {
		return destinationDir;
	}

	public FileFinder getFinder() {
		return finder;
	}

	public ComplexityCalculator getComplexity() {
		return complexity;
	}

	public String getEncoding() {
		return encoding;
	}
}
