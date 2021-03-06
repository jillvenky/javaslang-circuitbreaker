package io.github.robwin.retry.internal;

import io.github.robwin.retry.AsyncRetry;
import io.github.robwin.retry.RetryConfig;
import io.github.robwin.test.AsyncHelloWorldService;
import javaslang.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;

import javax.xml.ws.WebServiceException;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class AsyncRetryTest {
    private static final long DEFAULT_TIMEOUT_SECONDS = 5;

    private AsyncHelloWorldService helloWorldService;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    @Before
    public void setUp(){
        helloWorldService = mock(AsyncHelloWorldService.class);
    }

    @Test
    public void shouldNotRetry() throws InterruptedException, ExecutionException, TimeoutException {
        // Given the HelloWorldService returns Hello world
        given(helloWorldService.returnHelloWorld()).willReturn(completedFuture("Hello world"));
        // Create a Retry with default configuration
        AsyncRetry retryContext = AsyncRetry.ofDefaults("id");
        // Decorate the invocation of the HelloWorldService
        Supplier<CompletionStage<String>> supplier = AsyncRetry.decorateCompletionStage(
                retryContext,
                scheduler,
                () -> helloWorldService.returnHelloWorld());

        // When
        String result = awaitResult(supplier);
        // Then the helloWorldService should be invoked 1 time
        BDDMockito.then(helloWorldService).should(times(1)).returnHelloWorld();
        assertThat(result).isEqualTo("Hello world");
    }

    @Test
    public void shouldRetryInCaseOfExceptionAtSyncStage() {
        // Given the HelloWorldService throws an exception
        given(helloWorldService.returnHelloWorld())
                .willThrow(new WebServiceException("BAM!"))
                .willReturn(completedFuture("Hello world"));

        // Create a Retry with default configuration
        AsyncRetry retryContext = AsyncRetry.ofDefaults("id");
        // Decorate the invocation of the HelloWorldService
        Supplier<CompletionStage<String>> supplier = AsyncRetry.decorateCompletionStage(
                retryContext,
                scheduler,
                () -> helloWorldService.returnHelloWorld());

        // When
        String result = awaitResult(supplier.get());

        // Then the helloWorldService should be invoked 2 times
        BDDMockito.then(helloWorldService).should(times(2)).returnHelloWorld();
        assertThat(result).isEqualTo("Hello world");
    }

    @Test
    public void shouldRetryInCaseOfAnExceptionAtAsyncStage() {
        // Given the HelloWorldService throws an exception
        given(helloWorldService.returnHelloWorld())
                .willReturn(supplyAsync(() -> { throw new WebServiceException("BAM!"); }))
                .willReturn(completedFuture("Hello world"));

        // Create a Retry with default configuration
        AsyncRetry retryContext = AsyncRetry.ofDefaults("id");
        // Decorate the invocation of the HelloWorldService
        Supplier<CompletionStage<String>> supplier = AsyncRetry.decorateCompletionStage(
                retryContext,
                scheduler,
                () -> helloWorldService.returnHelloWorld());

        // When
        String result = awaitResult(supplier.get());

        // Then the helloWorldService should be invoked 2 times
        BDDMockito.then(helloWorldService).should(times(2)).returnHelloWorld();
        assertThat(result).isEqualTo("Hello world");
    }

    @Test
    public void shouldCompleteFutureAfterOneAttemptInCaseOfExceptionAtSyncStage() {
        shouldCompleteFutureAfterAttemptsInCaseOfExceptionAtSyncStage(1);
    }

    @Test
    public void shouldCompleteFutureAfterTwoAttemptsInCaseOfExceptionAtSyncStage() {
        shouldCompleteFutureAfterAttemptsInCaseOfExceptionAtSyncStage(2);
    }

    @Test
    public void shouldCompleteFutureAfterThreeAttemptsInCaseOfExceptionAtSyncStage() {
        shouldCompleteFutureAfterAttemptsInCaseOfExceptionAtSyncStage(3);
    }

    private void shouldCompleteFutureAfterAttemptsInCaseOfExceptionAtSyncStage(int noOfAttempts) {
        // Given the HelloWorldService throws an exception
        given(helloWorldService.returnHelloWorld())
                .willThrow(new WebServiceException("BAM!"));

        // Create a Retry with default configuration
        AsyncRetry retryContext = AsyncRetry.of(
                "id",
                RetryConfig
                        .custom()
                        .maxAttempts(noOfAttempts)
                        .build());
        // Decorate the invocation of the HelloWorldService
        Supplier<CompletionStage<String>> supplier = AsyncRetry.decorateCompletionStage(
                retryContext,
                scheduler,
                () -> helloWorldService.returnHelloWorld());

        // When
        Try<String> resultTry = Try.of(() -> awaitResult(supplier.get()));

        // Then the helloWorldService should be invoked n + 1  times
        BDDMockito.then(helloWorldService).should(times(noOfAttempts + 1)).returnHelloWorld();
        assertThat(resultTry.isFailure()).isTrue();
        assertThat(resultTry.getCause().getCause()).isInstanceOf(WebServiceException.class);
    }

    @Test
    public void shouldCompleteFutureAfterOneAttemptInCaseOfExceptionAtAsyncStage() {
        shouldCompleteFutureAfterAttemptsInCaseOfExceptionAtAsyncStage(1);
    }

    @Test
    public void shouldCompleteFutureAfterTwoAttemptsInCaseOfExceptionAtAsyncStage() {
        shouldCompleteFutureAfterAttemptsInCaseOfExceptionAtAsyncStage(2);
    }

    @Test
    public void shouldCompleteFutureAfterThreeAttemptsInCaseOfExceptionAtAsyncStage() {
        shouldCompleteFutureAfterAttemptsInCaseOfExceptionAtAsyncStage(3);
    }

    private void shouldCompleteFutureAfterAttemptsInCaseOfExceptionAtAsyncStage(int noOfAttempts) {
        // Given the HelloWorldService throws an exception
        given(helloWorldService.returnHelloWorld())
                .willReturn(supplyAsync(() -> { throw new WebServiceException("BAM!"); }));

        // Create a Retry with default configuration
        AsyncRetry retryContext = AsyncRetry.of(
                "id",
                RetryConfig
                        .custom()
                        .maxAttempts(noOfAttempts)
                        .build());
        // Decorate the invocation of the HelloWorldService
        Supplier<CompletionStage<String>> supplier = AsyncRetry.decorateCompletionStage(
                retryContext,
                scheduler,
                () -> helloWorldService.returnHelloWorld());

        // When
        Try<String> resultTry = Try.of(() -> awaitResult(supplier.get()));

        // Then the helloWorldService should be invoked n + 1 times
        BDDMockito.then(helloWorldService).should(times(noOfAttempts + 1)).returnHelloWorld();
        assertThat(resultTry.isFailure()).isTrue();
        assertThat(resultTry.getCause().getCause()).isInstanceOf(WebServiceException.class);
    }

    private static class RuntimeExecutionException extends RuntimeException {
        RuntimeExecutionException(Throwable cause) {
            super(cause);
        }
    }

    private static <T> T awaitResult(CompletionStage<T> completionStage, long timeoutSeconds) {
        try {
            return completionStage.toCompletableFuture().get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            throw new AssertionError(e);
        } catch (ExecutionException e) {
            throw new RuntimeExecutionException(e.getCause());
        }
    }

    private static <T> T awaitResult(CompletionStage<T> completionStage) {
        return awaitResult(completionStage, DEFAULT_TIMEOUT_SECONDS);
    }

    private static <T> T awaitResult(Supplier<CompletionStage<T>> completionStageSupplier, long timeoutSeconds) {
        return awaitResult(completionStageSupplier.get(), timeoutSeconds);
    }

    private static <T> T awaitResult(Supplier<CompletionStage<T>> completionStageSupplier) {
        return awaitResult(completionStageSupplier, DEFAULT_TIMEOUT_SECONDS);
    }

}
