package org.kframework.backend.java.builtins;

import org.kframework.backend.java.kil.BuiltinList;
import org.kframework.backend.java.kil.Term;
import org.kframework.backend.java.kil.TermContext;


/**
 * Table of {@code public static} methods on builtin fixed precision integers.
 *
 * @author AndreiS
 */
@SuppressWarnings("unchecked")
public class BuiltinBitVectorOperations {

    public static BitVector construct(IntToken bitwidth, IntToken value, TermContext context) {
        try {
            return BitVector.of(value.bigIntegerValue(), bitwidth.intValue());
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static IntToken bitwidth(BitVector term, TermContext context) {
        return IntToken.of(term.bitwidth());
    }

    public static BoolToken zero(BitVector term, TermContext context) {
        return BoolToken.of(term.isZero());
    }

    public static IntToken svalue(BitVector term, TermContext context) {
        return IntToken.of(term.signedValue());
    }

    public static IntToken uvalue(BitVector term, TermContext context) {
        return IntToken.of(term.unsignedValue());
    }

    public static BitVector add(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.add(term2);
        } else {
            return (BitVector) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BitVector sub(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.sub(term2);
        } else {
            return (BitVector) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BitVector mul(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.mul(term2);
        } else {
            return (BitVector) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BuiltinList sadd(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.sadd(term2);
        } else {
            return (BuiltinList) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BuiltinList uadd(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.uadd(term2);
        } else {
            return (BuiltinList) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BuiltinList ssub(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.ssub(term2);
        } else {
            return (BuiltinList) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BuiltinList usub(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.usub(term2);
        } else {
            return (BuiltinList) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BuiltinList smul(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.smul(term2);
        } else {
            return (BuiltinList) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BuiltinList umul(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.umul(term2);
        } else {
            return (BuiltinList) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static Term sdiv(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.sdiv(term2);
        } else {
            return (Term) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static Term udiv(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.udiv(term2);
        } else {
            return (Term) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static Term srem(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.srem(term2);
        } else {
            return (Term) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static Term urem(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.urem(term2);
        } else {
            return (Term) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BitVector and(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.and(term2);
        } else {
            return (BitVector) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BitVector or(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.or(term2);
        } else {
            return (BitVector) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BitVector xor(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.xor(term2);
        } else {
            return (BitVector) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken slt(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.slt(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken ult(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.ult(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken sle(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.sle(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken ule(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.ule(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken sgt(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.sgt(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken ugt(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.ugt(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken sge(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.sge(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken uge(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.uge(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BoolToken eq(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.eq(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    public static BuiltinList toDigits(BitVector term1, IntToken term2, TermContext context) {
        return new BuiltinList(term1.toDigits(term2.intValue()));
    }

    public static BitVector fromDigits(BitVector term1, BitVector term2, TermContext context) {
        if (term1.bitwidth() == term2.bitwidth()) {
            return term1.ne(term2);
        } else {
            return (BoolToken) failWithBitwidthMismatch(term1, term2);
        }
    }

    /**
     * Throws {@link IllegalArgumentException}.
     * @return does not return anything; it is not void as a convenience.
     */
    private static Object failWithBitwidthMismatch(BitVector term1, BitVector term2)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(
                "mismatch bit width: "
                + "first argument is represented on " + term1.bitwidth() + " bits "
                + "while second argument is represented on " + term2.bitwidth() + "bits");
    }

}
