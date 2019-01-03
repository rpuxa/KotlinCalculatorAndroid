package ru.rpuxa.kotlincalculatorandroid.parcer;

import ru.rpuxa.kotlincalculatorandroid.parcer.parts.step1.Argument;

public class Run {

    public static void main() {
        Argument arg = Expression.Companion
                .parse("e ^ (2x)", false).optimize();

        final Argument integrate = Integrals.INSTANCE.integrate(arg);
        if (integrate != null) {
            final String x = integrate.postOptimize().toString();
            System.out.println(x);
        }
        else
            System.out.println("null");
    }
}
