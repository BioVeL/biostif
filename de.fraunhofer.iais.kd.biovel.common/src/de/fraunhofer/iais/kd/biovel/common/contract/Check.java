package de.fraunhofer.iais.kd.biovel.common.contract;

public class Check {

    public static final String INTERNAL_CHECK_FAILED = "internal check failed";
    public static final String INTERNAL_CHECK_FAILMSG = INTERNAL_CHECK_FAILED + ": ";

    public static void isTrue(boolean condition) {
        if (!condition) {
            throw new CheckFailedException(INTERNAL_CHECK_FAILED);
        }
    }

    public static void isTrue(boolean condition, String failMsg) {
        if (!condition) {
            String msg = INTERNAL_CHECK_FAILMSG + failMsg;
            throw new CheckFailedException(msg);
        }
    }

    public static void isFalse(boolean condition) {
        if (condition) {
            throw new CheckFailedException(INTERNAL_CHECK_FAILED);
        }
    }

    public static void isFalse(boolean condition, String failMsg) {
        if (condition) {
            String msg = INTERNAL_CHECK_FAILMSG + failMsg;
            throw new CheckFailedException(msg);
        }
    }

    public static void isEqual(int expected, int actual) {
        if (!(expected == actual)) {
            String msg = INTERNAL_CHECK_FAILMSG + "isEqual expected=" + expected + ", actual=" + actual;
            throw new CheckFailedException(msg);
        }
    }

    public static void isEqual(String expected, String actual) {
        if (expected != actual) {
            if ((expected == null) || (actual == null) || !expected.equals(actual)) {
                String msg =
                    INTERNAL_CHECK_FAILMSG + "isEqual expected=\"" + expected + "\", actual=\"" + actual + "\"";
                throw new CheckFailedException(msg);
            }
        }
    }

    public static void isSame(Object expected, Object actual) {
        if (!(expected == actual)) {
            String msg = INTERNAL_CHECK_FAILMSG + "isSame expected=" + expected + ", actual=" + actual;
            throw new CheckFailedException(msg);
        }
    }

    public static void notYetImplemented() {
        fail(INTERNAL_CHECK_FAILMSG + "not yet implemented");
    }

    public static void notYetImplemented(String msg) {
        fail(INTERNAL_CHECK_FAILMSG + "not yet implemented: " + msg);
    }

    public static void not(boolean condition, String failMsg) {
        isTrue(!condition, failMsg);
    }

    public static void not(boolean condition) {
        isTrue(!condition);
    }

    public static void notNull(Object o, String failMsg) {
        isTrue(o != null, failMsg);
    }

    public static void notNull(Object o) {
        isTrue(o != null);
    }

    public static void isNull(Object o) {
        isTrue(o == null);
    }

    public static void isNull(Object o, String failMsg) {
        isTrue(o == null, failMsg);
    }

    public static void fail(String failMsg) {
        isTrue(false, failMsg);
    }

    public static void fail() {
        isTrue(false);
    }

    /**
     * prüft, dass arg nicht null ist. <br>
     * Die Methode soll zur Prüfung von Parametern verwendet werden.
     * 
     * @throws IllegalArgumentException falls die Prüfung fehlschlägt.
     * @param arg wird auf not null geprüft
     * @param caption wird in die Log- die Exception Meldung eingesetzt.
     */
    public static void argNotNull(Object arg, String caption) {
        if (arg == null) {
            String msg = INTERNAL_CHECK_FAILMSG + "null argument not expected. " + caption;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * prüft die Implikation: antezedens => sukzedens
     * 
     * @param antezedens
     * @param sukzedens
     */
    public static void implies(boolean antezedens, boolean sukzedens) {
        implies(antezedens, sukzedens, "implication failed: " + antezedens + " => " + sukzedens);
    }

    /**
     * prüft die Implikation: antezedens => sukzedens
     * 
     * @param antezedens
     * @param sukzedens
     * @param failMsg anzuzeigende Fehlermeldung
     */
    public static void implies(boolean antezedens, boolean sukzedens, String failMsg) {
        Check.isTrue((!antezedens) || sukzedens, failMsg);
    }

    public static void argNotNull(Object arg) {
        argNotNull(arg, "");
    }

    /**
     * prüft, dass die Zeichenkette <code>str</code> zu einer Integer Zahl
     * übersetzt werden kann. <br>
     * Die Methode soll zur Prüfung von Parametern verwendet werden.
     * 
     * @throws IllegalArgumentException falls die Prüfung fehlschlägt.
     * @param str wird auf ein gültiges Integer-Format geprüft.
     * @param caption wird in die Log- die Exception Meldung eingesetzt.
     */
    public static void argIsInteger(String str, String caption) {
        argNotNull(str, caption);
        argIsInstanceOf(str, caption, String.class);
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            final String msg = INTERNAL_CHECK_FAILMSG + caption + " is not an integer";
            throw new IllegalArgumentException(msg, e);
        }
    }

    public static void argIsInteger(String str) {
        argIsInteger(str, "");
    }

    /**
     * prüft, dass <code>object</code> ein Exemplar der Klasse
     * <code>clazz</code> ist. <br>
     * Die Methode soll zur Prüfung von Parametern verwendet werden.
     * 
     * @throws IllegalArgumentException falls die Prüfung fehlschlägt.
     * @param object wird auf die Zugehörigkeit zur Klasse <code>clazz</code>
     *            geprüft.
     * @param caption wird in die Log- die Exception Meldung eingesetzt.
     * @param clazz <code>object</code> soll ein Exemplar dieser Klasse sein.
     */
    public static void argIsInstanceOf(Object object, String caption, Class<?> clazz) {
        if (!clazz.isInstance(object)) {
            final String msg = INTERNAL_CHECK_FAILMSG + caption + " must be instance of " + clazz.getName();
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * prüft, dass <code>clazz.isAssignableFrom(classOfArg)</code> gilt. <br>
     * Die Methode soll zur Prüfung von Parametern verwendet werden.
     * 
     * @throws IllegalArgumentException falls die Prüfung fehlschlägt.
     * @param clazz
     * @param classOfArg
     */
    public static void canBeAssignedWith(Class<?> clazz, Class<?> classOfArg) {
        if (!clazz.isAssignableFrom(classOfArg)) {
            throw new IllegalArgumentException("a variable of type " + clazz.getName() + " cannot be assigned with "
                    + classOfArg.getName());
        }
    }

    /**
     * prüft, ob <code>o</code> ungleich <code>null</code> und ein Exemplar der
     * Klasse <code>clazz</code> ist.
     * 
     * @throws CheckFailedException falls die Prüfung fehlschlägt
     * @param o
     * @param clazz
     */
    public static void isInstanceOf(Object o, Class<?> clazz) {
        Check.notNull(clazz);
        if (o == null) {
            throw new CheckFailedException("expected: object of class " + clazz.getName() + " actual is: null");
        } else {
            if (!clazz.isInstance(o)) {
                throw new CheckFailedException("expected: object of class " + clazz.getName()
                        + " actual is: object of " + o.getClass().getName());
            }
        }
    }

    /**
     * fail in Einschubmethoden, die in Unterklassen redefiniert werden müssen.
     */
    public static void subclassResponsibility() {
        fail("this method must be overwritten in subclasses");
    }

}
