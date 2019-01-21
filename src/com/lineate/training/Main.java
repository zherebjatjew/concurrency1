package com.lineate.training;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Encoder {
    void eval(String value, List<String> receiver) {
        // There is no point in paralleling it further: the logic is too fast and
        // does not refer to any external resources.
        receiver.addAll(Arrays.asList(
                "[EAN-13] " + value,
                "[UPC] " + value,
                "[PDF417] " + value,
                "[QR] " + value,
                "[RFID] " + value
        ));
    }
}

class Printer implements Runnable {
    private static Logger LOG = Logger.getLogger("Printer");
    private int pos = 1000;
    private List<String> buffer;

    Printer(List<String> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while (pos <= 5000) {
            if (buffer.size() >= pos) {
//                        LOG.info(Integer.toString(pos));
                for (int i = pos - 1000; i < pos; i++) {
                    LOG.info(buffer.get(i));
                }
                pos += 1000;
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        LOG.info("DONE");
    }
}

public class Main {
    private static Logger LOG = Logger.getLogger("Encoder");

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Encoder e = new Encoder();
        List<String> result = Collections.synchronizedList(new ArrayList<>(5000));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future printed = executor.submit(new Printer(result));
        Stream<String> source = IntStream.rangeClosed(1, 1000).mapToObj(Integer::toString);
        long startTime = new Date().getTime();
        source.parallel()
              .unordered()
              .forEach(n -> e.eval(n, result));
        long endTime = new Date().getTime();
        printed.get();
        executor.shutdown();
        LOG.info(String.format("Execution time: %d ms", endTime - startTime));
    }
}
