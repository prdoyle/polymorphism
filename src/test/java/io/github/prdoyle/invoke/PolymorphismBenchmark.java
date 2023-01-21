package io.github.prdoyle.invoke;

import java.util.Random;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import static org.openjdk.jmh.annotations.Mode.Throughput;

@Fork(0)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
public class PolymorphismBenchmark {
	@State(Scope.Benchmark)
	public static class BenchmarkState {
		TheInterface[] targets = new TheInterface[2];
		TheInterface[] sequence = new TheInterface[2520]; // LCM of everything from 1 to 10

		@Setup(Level.Trial)
		public void setup() {
			targets[0] = new Implementation0();
			targets[1] = new Implementation1();
			Random random = new Random(123);
			for (int i = 0; i < sequence.length; i++) {
				sequence[i] = targets[random.nextInt(targets.length)];
			}
		}

		private static final class Implementation0 implements TheInterface {
			@Override public int theMethod(int arg) { return methodBody(arg); }

			private static int methodBody(int arg) {
				return arg+1;
			}
		}

		private static final class Implementation1 implements TheInterface {
			@Override public int theMethod(int arg) { return methodBody(arg); }

			static int methodBody(int arg) {
				return arg+2;
			}
		}
	}

	@Benchmark
	@BenchmarkMode(Throughput)
	public int invocation(BenchmarkState state) {
		int result = 0;
		for (int i = 0; i < state.sequence.length; i++) {
			result += state.sequence[i].theMethod(result);
		}
		return result;
	}

	@Benchmark
	@BenchmarkMode(Throughput)
	public int ifStatement(BenchmarkState state) {
		int result = 0;
		for (int i = 0; i < state.sequence.length; i++) {
			TheInterface target = state.sequence[i];
			if (target.getClass() == BenchmarkState.Implementation0.class) {
				result = BenchmarkState.Implementation0.methodBody(result);
			} else {
				result = BenchmarkState.Implementation1.methodBody(result);
			}
		}
		return result;
	}

}
