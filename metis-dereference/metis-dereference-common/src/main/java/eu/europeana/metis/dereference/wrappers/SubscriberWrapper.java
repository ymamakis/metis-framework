package eu.europeana.metis.dereference.wrappers;

import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SubscriberWrapper implements BodySubscriber<String> {

  private static final Logger LOG = LoggerFactory.getLogger(SubscriberWrapper.class);

  private final CountDownLatch latch;
  private final BodySubscriber<String> subscriber;
  private Subscription subscription;

  SubscriberWrapper(BodySubscriber<String> subscriber, CountDownLatch latch) {
    this.subscriber = subscriber;
    this.latch = latch;
  }

  @Override
  public CompletionStage<String> getBody() {
    return subscriber.getBody();
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    subscriber.onSubscribe(subscription);
    this.subscription = subscription;
    latch.countDown();
  }

  @Override
  public void onNext(List<ByteBuffer> item) {
    subscriber.onNext(item);
  }

  @Override
  public void onError(Throwable throwable) {
    subscriber.onError(throwable);
  }

  @Override
  public void onComplete() {
    subscriber.onComplete();
  }

  public void cancel() {
    subscription.cancel();
    LOG.debug("Subscription got cancelled");
  }
}


