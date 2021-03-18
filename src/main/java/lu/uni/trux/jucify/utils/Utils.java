package lu.uni.trux.jucify.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.jimple.Jimple;

/*-
 * #%L
 * JuCify
 * 
 * %%
 * Copyright (C) 2021 Jordan Samhi
 * University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - TruX - All rights reserved
 *
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class Utils {

	private static Map<String, String> compactTypesToJimpleTypes = null;

	public static String removeNodePrefix(String s) {
		if(s.startsWith(Constants.NODE_PREFIX)) {
			return s.substring(5, s.length());
		}
		return s;
	}

	public static SootMethodRef getMethodRef(String className, String methodName) {
		return Scene.v().getSootClass(className).getMethod(methodName).makeRef();
	}

	public static Unit addMethodCall(SootMethod caller, SootMethod callee) {
		Body b = caller.retrieveActiveBody();
		final PatchingChain<Unit> units = b.getUnits();
		Local thisLocal = b.getThisLocal();
		Unit newUnit = Jimple.v().newInvokeStmt(
				Jimple.v().newSpecialInvokeExpr(thisLocal,
						Utils.getMethodRef(Constants.DUMMY_BINARY_CLASS, callee.getSubSignature())));
		units.insertBefore(newUnit, units.getLast());
		return newUnit;
	}

	public static Pair<String, String> compactSigtoJimpleSig(String sig) {
		sig = sig.trim();
		String[] split = sig.split("\\)");
		String ret = split[1];
		String[] splitSplit = split[0].split("\\(");
		String params = null;
		String currentType = null;
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		if(splitSplit.length != 0) {
			params = splitSplit[1];
			String[] splitParams = params.split(" ");
			for(int i = 0 ; i < splitParams.length ; i++) {
				currentType = splitParams[i];
				sb.append(getCompactTypesToJimpleTypes(currentType));
				if(i != splitParams.length - 1) {
					sb.append(",");
				}
			}
		}
		sb.append(")");
		ret = getCompactTypesToJimpleTypes(ret);
		return new Pair<String, String>(sb.toString(), ret);
	}

	private static String getCompactTypesToJimpleTypes(String key) {
		if(compactTypesToJimpleTypes == null) {
			compactTypesToJimpleTypes = new HashMap<String, String>();
			compactTypesToJimpleTypes.put("V", "void");
			compactTypesToJimpleTypes.put("Z", "boolean");
			compactTypesToJimpleTypes.put("B", "byte");
			compactTypesToJimpleTypes.put("C", "char");
			compactTypesToJimpleTypes.put("S", "short");
			compactTypesToJimpleTypes.put("I", "int");
			compactTypesToJimpleTypes.put("J", "long");
			compactTypesToJimpleTypes.put("F", "float");
			compactTypesToJimpleTypes.put("D", "double");
		}
		if(key.startsWith("L")) {
			return key.substring(1, key.length() - 1).replace("/", ".");
		}else if(key.startsWith("[")) {
			return String.format("%s[]", key.substring(1));
		}
		return compactTypesToJimpleTypes.get(key);
	}
	
	public static String toJimpleSignature(String clazz, String ret, String method, String params) {
		return String.format("<%s: %s %s%s>", clazz, ret, method, params);
	}
	
	public static String getClassNameFromSignature(String sig) {
		String tmp = sig.split(" ")[0];
		return tmp.substring(1, tmp.length() - 1);
	}

	public static String getMethodNameFromSignature(String sig) {
		String tmp = sig.split(" ")[2];
		return tmp.substring(0, tmp.indexOf("("));
	}

	public static String getReturnNameFromSignature(String sig) {
		return sig.split(" ")[1];
	}

	public static List<String> getParametersNamesFromSignature(String sig) {
		String tmp = sig.split(" ")[2];
		String params = tmp.substring(tmp.indexOf("(") + 1, tmp.indexOf(")"));
		String[] paramsArray = params.split(",");
		List<String> parameters = new ArrayList<String>();
		for(int i = 0 ; i < paramsArray.length ; i++) {
			parameters.add(paramsArray[i]);
		}
		return parameters;
	}
	
	public static boolean isFromNativeCode(SootMethod sm) {
		if(sm.getDeclaringClass().equals(Scene.v().getSootClass(Constants.DUMMY_BINARY_CLASS))) {
			return true;
		}
		return false;
	}
}