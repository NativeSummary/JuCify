package lu.uni.trux.jucify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParserException;

import lu.uni.trux.jucify.files.SourcesSinksManager;
import lu.uni.trux.jucify.utils.Constants;
import lu.uni.trux.jucify.utils.CustomPrints;
import lu.uni.trux.jucify.utils.Utils;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;

public class FlowAnalysis {

	private SetupApplication sa;
	private InfoflowCFG icfg;
	private boolean raw;

	public FlowAnalysis(SetupApplication sa, boolean raw) {
		this.sa = sa;
		this.icfg = new InfoflowCFG();
		this.raw = raw;
	}

	public void run() {
		final ITaintPropagationWrapper taintWrapper;
		EasyTaintWrapper easyTaintWrapper = null;
		InputStream resource = Main.class.getResourceAsStream(Constants.EASY_TAINT_WRAPPER_FILE);
		File twFile = null;
		try {
			twFile = new File(String.format("%s%s", Constants.TARGET_TMP_DIR, Constants.EASY_TAINT_WRAPPER_FILE));
			FileUtils.copyInputStreamToFile(resource, twFile);
		} catch (IOException e) {
			CustomPrints.perror(e.getMessage());
		}
		if (twFile.exists())
			try {
				easyTaintWrapper = new EasyTaintWrapper(twFile);
			} catch (IOException e) {
				CustomPrints.perror(e.getMessage());
			}
		else {
			if(!this.raw) {
				CustomPrints.perror("Taint wrapper definition file not found at "
						+ twFile.getAbsolutePath());
			}
		}
		easyTaintWrapper.setAggressiveMode(true);
		taintWrapper = easyTaintWrapper;
		sa.setTaintWrapper(taintWrapper);
		InfoflowResults results = null;
		try {
			results = sa.runInfoflow(SourcesSinksManager.v().getSources(), SourcesSinksManager.v().getSinks());
		} catch (IOException e) {
			CustomPrints.perror(e.getMessage());
		} catch (XmlPullParserException e) {
			CustomPrints.perror(e.getMessage());
		}
		if(results != null) {
			if(results.getResults() != null && !results.getResults().isEmpty()) {
				for (ResultSinkInfo sink : results.getResults().keySet()) {
					for (ResultSourceInfo source : results.getResults().get(sink)) {
						List<Stmt> path = Arrays.asList(source.getPath());
						try {
							if(pathContainCallToNativeMethods(path)) {
								if (path != null && !path.isEmpty()) {
									ResultsAccumulator.v().setHasFlowThroughNative(true);
									if(!this.raw) {
										CustomPrints.psuccess("Found path through native code: ");
										System.out.println("  - From " + source);
										System.out.println("    - Detailed path:");
										for(Stmt s : path) {
											System.out.println("       " + s + " => in method: " + icfg.getMethodOf(s));
										}
										System.out.println("  - To " + sink);
									}
								}
							}
						} catch (Exception e) {
							CustomPrints.perror(e.getMessage());
						}
					}
				}
			}else {
				if(!this.raw) {
					CustomPrints.pwarning("No Flow found.");
				}
			}
		}
	}

	private boolean pathContainCallToNativeMethods(List<Stmt> path) {
		for(Stmt s: path) {
			if(Utils.isFromNativeCode(icfg.getMethodOf(s))) {
				return true;
			}
		}
		return false;
	}

}
