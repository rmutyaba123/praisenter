package org.praisenter.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

import org.praisenter.ThrowableConsumer;
import org.praisenter.ThrowableFunction;
import org.praisenter.ThrowableRunnable;

import javafx.application.Platform;

public final class AsyncHelper {
	private AsyncHelper() {}

	public static <T> Function<T, CompletableFuture<Void>> onJavaFXThreadAndWait(final ThrowableConsumer<T> operation) {
		return (T data) -> {
			CompletableFuture<Void> future = new CompletableFuture<Void>();
			
			if (Platform.isFxApplicationThread()) {
			       try {
			           operation.accept(data);
			           future.complete(null);
			       } catch (Throwable t) {
			           future.completeExceptionally(t);
			       }
			  } else {
			      Platform.runLater(() -> {
			          try {
			              operation.accept(data);
			              future.complete(null);
			          } catch (Throwable t) {
			              future.completeExceptionally(t);
			          }
			      });
			  }
			
			return future;
		};
	}
	
	public static <T, E> Function<T, CompletableFuture<E>> onJavaFXThreadAndWait(final ThrowableFunction<T, E> operation) {
		return (T data) -> {
			CompletableFuture<E> future = new CompletableFuture<E>();
			
			if (Platform.isFxApplicationThread()) {
			       try {
			           future.complete(operation.apply(data));
			       } catch (Throwable t) {
			           future.completeExceptionally(t);
			       }
			  } else {
			      Platform.runLater(() -> {
			          try {
			        	  future.complete(operation.apply(data));
			          } catch (Throwable t) {
			              future.completeExceptionally(t);
			          }
			      });
			  }
			
			return future;
		};
	}
	
	public static <T> Function<T, CompletableFuture<Void>> onJavaFXThreadAndWait(final ThrowableRunnable operation) {
		return (T data) -> {
			CompletableFuture<Void> future = new CompletableFuture<Void>();
			
			if (Platform.isFxApplicationThread()) {
			       try {
			    	   operation.run();
			           future.complete(null);
			       } catch (Throwable t) {
			           future.completeExceptionally(t);
			       }
			  } else {
			      Platform.runLater(() -> {
			          try {
			        	  operation.run();
			        	  future.complete(null);
			          } catch (Throwable t) {
			              future.completeExceptionally(t);
			          }
			      });
			  }
			
			return future;
		};
	}

	public static void onJavaFXThread(Runnable operation) {
		if (Platform.isFxApplicationThread()) {
			operation.run();
		} else {
			Platform.runLater(operation);
		}
	}

	public static List<Throwable> getExceptions(CompletableFuture<?>[] futures) {
		return AsyncHelper.getExceptions(Arrays.asList(futures));
	}
	
	public static List<Throwable> getExceptions(Iterable<CompletableFuture<?>> futures) {
		List<Throwable> exceptions = new ArrayList<>();
		for (CompletableFuture<?> future : futures) {
			try {
				future.join();
			} catch (CompletionException ex) {
				// handle re-wrapped completion exceptions
				Throwable t = ex.getCause();
				while (t instanceof CompletionException) {
					t = t.getCause();
				}
				exceptions.add(t);
			} catch (Exception ex) {
				exceptions.add(ex);
			}
		}
		return exceptions;
	}
}
