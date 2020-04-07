package com.waitingforcode

import org.apache.pulsar.client.admin.PulsarAdmin
import org.apache.pulsar.client.api.{Message, PulsarClient}

object TestProducer extends App {

  val admin = PulsarAdmin.builder()
    .serviceHttpUrl("http://localhost:8080")
    .tlsTrustCertsFilePath(null)
    .allowTlsInsecureConnection(false)
    .enableTlsHostnameVerification(false)
    .build()

  admin.topics().createNonPartitionedTopic("my-topic")

  val client = PulsarClient.builder()
    .serviceUrl("pulsar://localhost:6650")
    .build()

  new Thread(new Runnable() {
    override def run(): Unit = {
      val producer = client.newProducer()
        .topic("my-topic")
        .create()
      var nr = 0
      while (true) {
        producer.send(s"My message ${nr}".getBytes())
        println("Sending new message")
        nr += 1
        Thread.sleep(2000)
      }
    }
  }).start()

  val consumer = client.newConsumer()
    .topic("my-topic")
    .subscriptionName("my-topic-subscription")
    .consumerName("test-consumer")
    .subscribe()

  while (true) {
    val messages = consumer.batchReceive() //.asScala.toSeq
    messages.forEach((message: Message[Array[Byte]]) => {
      println(s"Got message=${new String(message.getData)}")
    })
    consumer.acknowledge(messages)
  }

}
