package io.easeci.worker.pipeline;

import io.easeci.worker.connect.state.NodeConnectionState;
import io.easeci.worker.connect.state.NodeProcessingState;
import io.easeci.worker.engine.DockerPlatformRunner;
import io.easeci.worker.engine.Runner;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Singleton
public class PipelineExecutionFacade {

    private final DockerPlatformRunner runner = new DockerPlatformRunner();

    public ScheduleResponse mockResponse() {
        return ScheduleResponse.builder()
                .nodeConnectionState(NodeConnectionState.ESTABLISHED)
                .nodeProcessingState(NodeProcessingState.BUSY)
                .pipelineReceivedTime(Instant.now().toEpochMilli())
                .scheduleErrorCode(null)
                .isSuccessfullyScheduled(true)
                .build();
    }

    public ScheduleResponse handlePipeline(ScheduleRequest scheduleRequest) throws IOException {
        byte[] scriptDecoded = Base64.getDecoder().decode(scheduleRequest.getScriptEncoded());

        Path pipelineContextDir = Files.createDirectories(Paths.get("/tmp/easeci-worker/".concat(scheduleRequest.getPipelineContextId().toString())));

        if (Files.exists(Path.of(pipelineContextDir.toString().concat("/pipeline-script.py")))) {
            Files.delete(Path.of(pipelineContextDir.toString().concat("/pipeline-script.py")));
        }
        Path file = Files.createFile(Path.of(pipelineContextDir.toString().concat("/pipeline-script.py")));
        Path executableFile = Files.write(file, scriptDecoded);

//        runner.execution(executableFile.toFile());

        runner.runContainer(pipelineContextDir);
        return mockResponse();
    }
}
