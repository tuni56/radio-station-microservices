package com.radiostation.program.api;

import com.radiostation.program.domain.Program;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/programs")
public class ProgramController {

    @PostMapping
    public Program createProgram(@RequestParam String name) {
        return new Program(UUID.randomUUID().toString(), name);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}

