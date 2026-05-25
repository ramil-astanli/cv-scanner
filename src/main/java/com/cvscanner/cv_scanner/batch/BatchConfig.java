package com.cvscanner.cv_scanner.batch;

import com.cvscanner.cv_scanner.entity.Candidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CvItemProcessor cvItemProcessor;
    private final CvItemWriter cvItemWriter;
    private final CvSkipListener cvSkipListener;
    private final JobCompletionListener jobCompletionListener;

    @Bean
    public Step cvProcessingStep() {
        return new StepBuilder("cvProcessingStep", jobRepository)
                .<File, Candidate>chunk(10, transactionManager)
                .reader(new CvItemReader())
                .processor(cvItemProcessor)
                .writer(cvItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .retry(Exception.class)
                .retryLimit(3)
                .listener(cvSkipListener)
                .build();
    }

    @Bean(name = "asyncJobLauncher")
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public Job cvProcessingJob() {
        return new JobBuilder("cvProcessingJob", jobRepository)
                .start(cvProcessingStep())
                .listener(jobCompletionListener)
                .build();
    }
}