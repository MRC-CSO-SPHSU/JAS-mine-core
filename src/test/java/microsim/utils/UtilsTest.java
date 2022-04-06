package microsim.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.factory.Nd4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.IntStream;

import static java.lang.StrictMath.*;

class UtilsTest {

    private Random generator;

    @Setter @Getter private int dps_ = 100;

    private record illDotProductResult(double @NonNull [] x, double @NonNull [] y, BigDecimal d, BigDecimal C) { }
    private record illSumResult(double @NonNull [] out, BigDecimal exact, BigDecimal condition) { }


    static double log2(final double value) { return log(value) / log(2); }// TODO see various math libraries

    static int[] rint(final double...x){// todo see numpy behaviour, improve input check
        // todo strange behaviour, have to cast to float got get int out
        return Arrays.stream(x).mapToInt(v -> round((float) v)).toArray();
    }

// see https://stackoverflow.com/questions/16216248/convert-java-number-to-bigdecimal-best-way for errors
    static BigDecimal bdDot(final double @NonNull [] a, final double @NonNull [] b){
        // length check
        val bdc = new ArrayList<BigDecimal>();
        for (int i = 0; i < a.length; i++) {
            val bda = BigDecimal.valueOf(a[i]);
            val bdb = BigDecimal.valueOf(b[i]);
            bdc.add(i, bda.multiply(bdb));
        }

        var sum = BigDecimal.valueOf(0);
        for (var bigDecimal : bdc)
            sum = sum.add(bigDecimal);

        return sum;
    }

    static BigDecimal bdSum(final double @NonNull [] a){
        var sum = BigDecimal.valueOf(0);
        for (var value : a) // todo read about presicion, rounding
            sum = sum.add(BigDecimal.valueOf(value));

        return sum;
    }

    double iPow(double x, int n) // integer power, x has any sign, n is ??
    {
        if (n == 0) return 1;
        val p = n % 2 == 0? n / 2 : n - 1;// any use of cached values x**n?
        val m = iPow(x, p);
        return n % 2 == 0? m * m: x * m;
    }

    @Contract("_, _ -> new") // todo read about contracts
    private @NotNull illDotProductResult illDotProduct(int n, double c) {
        return illDotProduct(n, c, 100);
    }

    @Contract("_, _, _ -> new")
    private @NotNull illDotProductResult illDotProduct(int n, double c, int dps){
        assert (n >= 6) : "Vector size must be >= 6";

        val n2 = (int) round((double)n / 2); // TODO see various math libraries
        val x = new double[n];
        val y = new double[n];

        val b = log2(c);

        val rval = new ArrayList<double[]>();
        for (var i = 0; i < 5; i++)
            rval.add(generator.doubles(n2).toArray());

        IntStream.range(0, rval.get(0).length).forEach(i -> rval.get(0)[i] *= b / 2);
        val e = rint(rval.get(0));
        e[0] = (int) round(b / 2);
        e[e.length - 1] = 0;

        IntStream.range(0, n2).forEach(i -> x[i] = (2 * rval.get(1)[i] - 1) * pow(2, e[i]));
        // todo replace pow with something proper since it's integer (always positive)?
        IntStream.range(0, n2).forEach(i -> y[i] = (2 * rval.get(2)[i] - 1) * pow(2, e[i]));

        BigDecimal val1 = new BigDecimal("37578975587.876876989").setScale(dps, RoundingMode.HALF_UP); // fixme we have to set the scale everywhere

        val lsp = Nd4j.linspace(b/2, 0, n - n2).linearView();
        val e_ = rint(IntStream.range(0, lsp.length()).mapToDouble(lsp::getDouble).toArray());
        for (var i = n2; i < n; i++){
            var xslice = Arrays.copyOfRange(x, 0, i + 1);
            var yslice = Arrays.copyOfRange(y, 0, i + 1); // check indices
            x[i] = (2 * rval.get(3)[i - n2] - 1) * pow(2, e_[i - n2]);
            y[i] = ((2 * rval.get(4)[i - n2] - 1) * pow(2, e_[i - n2]) - bdDot(xslice, yslice).doubleValue())/x[i];
            // is conversion OK?
        }

        val collection = new ArrayList<double[]>();
        collection.add(1, x);
        collection.add(2, y);
        Collections.shuffle(collection);
        val xp = collection.get(0); // no need for new vars
        val yp = collection.get(1);

        val d = bdDot(x, y);

        val absx = new double[n];
        IntStream.range(0, xp.length).forEach(i -> absx[i] = abs(xp[i]));
        val absy = new double[n];
        IntStream.range(0, yp.length).forEach(i -> absy[i] = abs(yp[i]));

        val C = BigDecimal.valueOf(2).multiply(bdDot(absx, absy)).divide(d.abs(), RoundingMode.HALF_UP);
        return new illDotProductResult(x, y, d, C);
    }

    private illSumResult illSum(int n, double c, int dps){
        val t = illDotProduct(n, c, dps);

        val x = t.x();
        val y = t.y();
        val C = t.C();

        val prod = IntStream.range(0, x.length).mapToDouble(i -> x[i] * y[i]).toArray();
        val err = IntStream.range(0, x.length).mapToDouble(i -> fma(x[i], y[i], -prod[i])).toArray();

        val res = Arrays.copyOf(prod, prod.length + err.length);
        System.arraycopy(err, 0, res, prod.length, err.length);

        Collections.shuffle(Arrays.asList(res));
        val out = res;

        //dps
        val exact = bdSum(out);

        val condition = C.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);

        return new illSumResult(out, exact, condition);
    }

    private illSumResult illSum(int n, double c){
        return illSum(n, c, 100);
    }

    @Disabled
    @Test
    void KBKSum() {
        this.generator = new Random(0);
        illDotProduct(1,2,3);
        illSum(1,2,3);
    }
}