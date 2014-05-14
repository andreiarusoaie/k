package org.kframework.backend.symbolic;

import com.microsoft.z3.*;
import org.junit.Test;
import org.kframework.backend.java.util.Z3Wrapper;

/**
 * Created by andreiarusoaie on 04/05/14.
 */
public class Z3Tests {

    @Test
    public void testZ3() {
        try {
            com.microsoft.z3.Context context = Z3Wrapper.newContext();
            Solver solver = context.MkSolver();


//            // formula
//            BoolExpr a = context.MkBoolConst("a");
//            IntExpr i = context.MkIntConst("i");
//            IntExpr j = context.MkIntConst("j");
//
//            BoolExpr eq = context.MkEq(a, context.MkLe(i, j));
//
//            solver.Assert(eq);
//            System.out.println(solver.Check());
//            System.out.println(solver.Model());
//            System.out.println(solver.toString());

            Expr e = context.ParseSMTLIB2File("/Users/andreiarusoaie/work/a.smt2", null, null, null, null);
            System.out.println(solver.Check());
            System.out.println(solver.Model());

        } catch (Z3Exception e) {
            e.printStackTrace();
        }

    }

}
