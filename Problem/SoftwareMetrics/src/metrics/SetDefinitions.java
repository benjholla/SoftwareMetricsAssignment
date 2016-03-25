package metrics;

import com.ensoftcorp.atlas.core.query.Attr.Edge;
import com.ensoftcorp.atlas.core.query.Attr.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;

/**
 * Common set definitions which are useful for program analysis
 * 
 * @author Tom Deering, Ben Holland
 */
public final class SetDefinitions {

	private SetDefinitions() {}

	/**
	 * Types which represent arrays of other types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q arrayTypes() {
		return Common.universe().nodesTaggedWithAny(XCSG.ArrayType).retainNodes();
	}

	/**
	 * Types which represent language primitive types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q primitiveTypes() {
		return Common.universe().nodesTaggedWithAny(XCSG.Primitive).retainNodes();
	}

	/**
	 * Summary invoke nodes, representing invocations on methods.
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q invokeNodes() {
		return Common.universe().nodesTaggedWithAny(Node.INVOKE).retainNodes();
	}

	/**
	 * Everything declared under any of the known API projects, if they are in
	 * the index.
	 */
	public static Q apis() {
		return CommonQueries.declarations(Common.universe().nodesTaggedWithAny(Node.LIBRARY), TraversalDirection.FORWARD).difference(arrayTypes(),
				primitiveTypes(), invokeNodes());
	}

	/**
	 * Methods defined in java.lang.Object, and all methods which override them
	 */
	public static Q objectMethodOverrides() {
		return Common.edges(Edge.OVERRIDES).reverse(
				CommonQueries.declarations(Common.typeSelect("java.lang", "Object"), TraversalDirection.FORWARD).nodesTaggedWithAny(XCSG.Method));
	}

	/**
	 * Everything in the universe which is part of the app (not part of the
	 * apis, or any "floating" nodes).
	 */
	public static Q app() {
		return Common.universe().difference(apis(), invokeNodes(), arrayTypes(), primitiveTypes());
	}

	/**
	 * All method nodes declared by the APIs.
	 */
	public static Q apiMethods() {
		return apis().nodesTaggedWithAny(XCSG.Method);
	}

	/**
	 * All variable nodes declared by the APIs.
	 */
	public static Q apiVariables() {
		return apis().nodesTaggedWithAny(XCSG.Variable);
	}

	/**
	 * All data flow nodes declared by the APIs.
	 */
	public static Q apiDFN() {
		return apis().nodesTaggedWithAny(XCSG.DataFlow_Node);
	}

	/**
	 * All edges for which both endpoints lay within the APIs.
	 */
	public static Q apiEdges() {
		return apis().induce(Common.universe());
	}
}