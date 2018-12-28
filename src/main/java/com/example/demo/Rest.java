package com.example.demo;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RestController
@RequestMapping("/api2")
@Slf4j
public class Rest {

    @Autowired
    private Tracer tracer;

    @GetMapping(path = "/test/{millisec}")
    @HystrixCommand(
                    commandProperties=
                                    {@HystrixProperty(
                                                    name="execution.isolation.thread.timeoutInMilliseconds",
                                                    value="800")},
                    fallbackMethod = "hystrixFall")
    public String testHystrix(@PathVariable int millisec) {

        heavyMethod(millisec);

        log.info("span:");
        log.info(tracer.getCurrentSpan().traceIdString());

        URI uri = URI.create("https://jsonplaceholder.typicode.com/posts?userId=1");

        String result = "Service4 " + new RestTemplate().getForObject(uri, String.class);

        return result;

    }

    public void heavyMethod(int millesec) {

        Span newSpan = tracer.createSpan("hyTst_Heavy4!");

        try {
            Thread.sleep(millesec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            newSpan.tag("srv_heavy", "HY_Heavy!");
            newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
            tracer.close(newSpan);
        }

    }

    private String hystrixFall(int millisec) {
        return "Sorry service4 is down at " + millisec + ". Please try again later";
    }

}
