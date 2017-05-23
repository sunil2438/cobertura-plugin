/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Nathan Wilson
 * Copyright (C) 2009 Charlie Squires
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.check;

import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.reporting.CoverageThresholdsReport;
import net.sourceforge.cobertura.reporting.ReportName;
import net.sourceforge.cobertura.util.Header;
import org.apache.oro.text.regex.MalformedPatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.StringTokenizer;

import static net.sourceforge.cobertura.check.CoverageResultEntry.CoverageType.BRANCH;

public class CheckCoverageMain {
	private static final Logger logger = LoggerFactory
			.getLogger(CheckCoverageMain.class);

	public CheckCoverageMain(String[] args) throws MalformedPatternException {
		int exitStatus = checkCoverage(args);
		System.exit(exitStatus);
	}

	private static int checkCoverageTypeStatusAndLogMessage(
			CoverageResultEntry entry, int branchStatus, int lineStatus) {
		if (entry.getCoverageType().equals(BRANCH)) {
			logger
					.error(entry.getName() +
						   " failed coverage check. Branch coverage rate of "+
						   percentage(entry.getCurrentCoverage()) +
					       "% is below " +
						   percentage(entry.getExpectedCoverage()) +
						   "%");
			return branchStatus;
		} else {
			logger
					.error(entry.getName() +
						   " failed coverage check. Line coverage rate of " +
						   percentage(entry.getCurrentCoverage()) +
					       "% is below " +
						   percentage(entry.getExpectedCoverage()) +
					       "%");
			return lineStatus;
		}
	}

	private static double inRangeAndDivideByOneHundred(
			String coverageRateAsPercentage) {
		return inRangeAndDivideByOneHundred(Integer
				.valueOf(coverageRateAsPercentage));
	}

	private static double inRangeAndDivideByOneHundred(
			int coverageRateAsPercentage) {
		if ((coverageRateAsPercentage >= 0)
				&& (coverageRateAsPercentage <= 100)) {
			return (double) coverageRateAsPercentage / 100;
		}
		throw new IllegalArgumentException("The value "
				+ coverageRateAsPercentage
				+ "% is invalid.  Percentages must be between 0 and 100.");
	}

    private static String percentage(double coverateRate) {
        BigDecimal decimal = new BigDecimal(coverateRate * 100);
        return decimal.setScale(1, BigDecimal.ROUND_DOWN).toString();
    }

	public static int checkCoverage(String[] args)
			throws MalformedPatternException {
		Header.print(System.out);

		ArgumentsBuilder builder = new ArgumentsBuilder();

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--branch")) {
				builder
						.setClassBranchCoverageThreshold(inRangeAndDivideByOneHundred(args[++i]));
			} else if (args[i].equals("--datafile")) {
				builder.setDataFile(args[++i]);
			} else if (args[i].equals("--line")) {
				builder
						.setClassLineCoverageThreshold(inRangeAndDivideByOneHundred(args[++i]));
			} else if (args[i].equals("--regex")) {
				StringTokenizer tokenizer = new StringTokenizer(args[++i], ":");
				builder.addMinimumCoverageRates(tokenizer.nextToken(),
						inRangeAndDivideByOneHundred(tokenizer.nextToken()),
						inRangeAndDivideByOneHundred(tokenizer.nextToken()));
			} else if (args[i].equals("--packagebranch")) {
				builder
						.setPackageBranchCoverageThreshold(inRangeAndDivideByOneHundred(args[++i]));
			} else if (args[i].equals("--packageline")) {
				builder
						.setPackageLineCoverageThreshold(inRangeAndDivideByOneHundred(args[++i]));
			} else if (args[i].equals("--totalbranch")) {
				builder
						.setTotalBranchCoverageThreshold(inRangeAndDivideByOneHundred(args[++i]));
			} else if (args[i].equals("--totalline")) {
				builder
						.setTotalLineCoverageThreshold(inRangeAndDivideByOneHundred(args[++i]));
			}
		}

		Arguments arguments = builder.build();

		CoverageThresholdsReport coverageThresholdsReport = (CoverageThresholdsReport) new Cobertura(
				arguments).checkThresholds().report().getByName(
				ReportName.THRESHOLDS_REPORT);

		List<CoverageResultEntry> coverageResultEntries = coverageThresholdsReport
				.getCoverageResultEntries();
		int exitStatus = 0;
		for (CoverageResultEntry entry : coverageResultEntries) {
			if (entry.isBelowExpectedCoverage()) {
				switch (entry.getCoverageLevel()) {
					case CLASS :
						exitStatus |= checkCoverageTypeStatusAndLogMessage(
								entry, 2, 4);
						break;
					case PACKAGE :
						exitStatus |= checkCoverageTypeStatusAndLogMessage(
								entry, 32, 64);
						break;
					case PROJECT :
						exitStatus |= checkCoverageTypeStatusAndLogMessage(
								entry, 8, 16);
						break;
				}
			}
		}
		return exitStatus;
	}

	public static void main(String[] args) throws MalformedPatternException {
		int exitStatus = checkCoverage(args);
		System.exit(exitStatus);
	}
}
