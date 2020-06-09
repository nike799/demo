package com.example.demo.controller;

import com.example.demo.engine.Engine;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {
    private final Engine engine;

    public Controller(Engine engine) {
        this.engine = engine;
    }

    @PostMapping(value = "/submission")
    public Engine.SubmissionResult sendSubmission(@RequestBody() String submission) throws IOException, InterruptedException {
        return this.engine.runEngine(submission);
    }
}
