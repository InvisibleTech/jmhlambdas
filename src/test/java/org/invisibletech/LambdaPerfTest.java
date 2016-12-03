package org.invisibletech;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class LambdaPerfTest {

	@State(Scope.Thread)
	public static class BenchmarkState {
		List<Double> list;

		@Setup(Level.Trial)
		public void initialize() {
			Random rand = new Random();

			list = DoubleStream.generate(() -> new Double(rand.nextDouble())).limit(500).boxed()
					.collect(Collectors.toList());
		}
	}

	@Test
	public void driveTest() throws RunnerException {
		Options options = new OptionsBuilder().include(this.getClass().getName() + ".*")
				// Set the following options as needed
				.mode(Mode.AverageTime)
				.timeUnit(TimeUnit.MICROSECONDS)
				.warmupTime(TimeValue.seconds(30))
				.warmupIterations(3)
				.measurementTime(TimeValue.seconds(30))
				.measurementIterations(10)
				.threads(1)
				.forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				// .jvmArgs("-XX:+UnlockDiagnosticVMOptions",
				// "-XX:+PrintInlining")
				// .addProfiler(WinPerfAsmProfiler.class)
				.build();

		new Runner(options).run();
	}

	@Test
	public void driveTestOn3Threads() throws RunnerException {
		Options options = new OptionsBuilder().include(this.getClass().getName() + ".*")
				// Set the following options as needed
				.mode(Mode.AverageTime)
				.timeUnit(TimeUnit.MICROSECONDS)
				.warmupTime(TimeValue.seconds(30))
				.warmupIterations(3)
				.measurementTime(TimeValue.seconds(30))
				.measurementIterations(10)
				.threads(3)
				.forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				// .jvmArgs("-XX:+UnlockDiagnosticVMOptions",
				// "-XX:+PrintInlining")
				// .addProfiler(WinPerfAsmProfiler.class)
				.build();

		new Runner(options).run();
	}

	@Benchmark
	public void lambdas(BenchmarkState state, Blackhole bh) {
		bh.consume(state.list.stream().map(d -> d.toString()).collect(Collectors.toList()));
	}

	static class Stringer implements Function<Double, String> {
		@Override
		public String apply(Double t) {
			return t.toString();
		}
	}

	@Benchmark
	public void anonStaticClasses(BenchmarkState state, Blackhole bh) {
		Stringer mapper = new Stringer();
		bh.consume(state.list.stream().map(mapper).collect(Collectors.toList()));
	}

	@Benchmark
	public void forEach(BenchmarkState state, Blackhole bh) {
		List<String> output = new ArrayList<>(state.list.size());
		for (Double val : state.list) {
			output.add(val.toString());
		}

		bh.consume(output);
	}
}
