package finance.tegro.api.configuration

import finance.tegro.api.entity.*
import finance.tegro.api.processor.*
import finance.tegro.api.repository.*
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.support.SimpleFlow
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.data.domain.Sort.Direction
import org.springframework.transaction.PlatformTransactionManager
import org.ton.block.MsgAddress
import java.time.Instant
import java.util.function.Function

@Configuration
class BatchConfiguration {
//    @Bean
//    fun jobRepositoryFactory(dataSource: DataSource, transactionManager: PlatformTransactionManager) =
//        JobRepositoryFactoryBean().apply {
//            setDataSource(dataSource)
//            setTransactionManager(transactionManager)
//        }

    @Bean
    fun updateExchangePairAdminStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        exchangePairRepository: ExchangePairRepository,
        appendLastBlockIdProcessor: AppendLastBlockIdProcessor<MsgAddress>,
        fetchExchangePairAdminProcessor: FetchExchangePairAdminProcessor,
        exchangePairAdminRepository: ExchangePairAdminRepository,
    ) =
        StepBuilder("updateExchangePairAdminStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<ExchangePair, ExchangePairAdmin>(1)
            .reader(
                RepositoryItemReaderBuilder<ExchangePair>()
                    .name("updateExchangePairAdminStepReader")
                    .methodName("findAll")
                    .repository(exchangePairRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(
                CompositeItemProcessorBuilder<ExchangePair, ExchangePairAdmin>()
                    .delegates(
                        ItemProcessor<ExchangePair, MsgAddress> { it.address },
                        appendLastBlockIdProcessor,
                        fetchExchangePairAdminProcessor,
                    )
                    .build()
            )
            .writer(
                RepositoryItemWriterBuilder<ExchangePairAdmin>()
                    .methodName("save")
                    .repository(exchangePairAdminRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateExchangePairTokenStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        exchangePairRepository: ExchangePairRepository,
        appendLastBlockIdProcessor: AppendLastBlockIdProcessor<MsgAddress>,
        fetchExchangePairTokenProcessor: FetchExchangePairTokenProcessor,
        exchangePairTokenRepository: ExchangePairTokenRepository,
    ) =
        StepBuilder("updateExchangePairTokenStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<ExchangePair, ExchangePairToken>(1)
            .reader(
                RepositoryItemReaderBuilder<ExchangePair>()
                    .name("updateExchangePairTokenStepReader")
                    .methodName("findAll")
                    .repository(exchangePairRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(
                CompositeItemProcessorBuilder<ExchangePair, ExchangePairToken>()
                    .delegates(
                        ItemProcessor<ExchangePair, MsgAddress> { it.address },
                        appendLastBlockIdProcessor,
                        fetchExchangePairTokenProcessor,
                    )
                    .build()
            )
            .writer(
                RepositoryItemWriterBuilder<ExchangePairToken>()
                    .methodName("save")
                    .repository(exchangePairTokenRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateExchangePairStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        exchangePairRepository: ExchangePairRepository,
    ) =
        StepBuilder("updateExchangePairStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<ExchangePair, ExchangePair>(1)
            .reader(
                RepositoryItemReaderBuilder<ExchangePair>()
                    .name("updateExchangePairStepReader")
                    .methodName("findAll")
                    .repository(exchangePairRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(
                Function { t ->
                    t.apply {
                        timestamp = Instant.now()
                    }
                }
            )
            .writer(
                RepositoryItemWriterBuilder<ExchangePair>()
                    .methodName("save")
                    .repository(exchangePairRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateExchangePairJob(
        jobRepository: JobRepository,
        taskExecutor: TaskExecutor,
        updateExchangePairAdminStep: Step,
        updateExchangePairTokenStep: Step,
        updateExchangePairStep: Step,
    ) = JobBuilder("updateExchangePairJob")
        .repository(jobRepository)
        .incrementer(RunIdIncrementer())
        .start(
            FlowBuilder<SimpleFlow>("updateExchangePairAdminAndUpdateExchangePairTokenFlow")
                .split(taskExecutor)
                .add(
                    FlowBuilder<SimpleFlow>("updateExchangePairAdminFlow")
                        .start(updateExchangePairAdminStep)
                        .build(),
                    FlowBuilder<SimpleFlow>("updateExchangePairTokenFlow")
                        .start(updateExchangePairTokenStep)
                        .build(),
                )
                .next(updateExchangePairStep)
                .build()
        )
        .build()
        .build()

    @Bean
    fun updateReserveStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        exchangePairRepository: ExchangePairRepository,
        appendLastBlockIdProcessor: AppendLastBlockIdProcessor<MsgAddress>,
        fetchReserveProcessor: FetchReserveProcessor,
        reserveRepository: ReserveRepository,
    ) =
        StepBuilder("updateReserveStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<ExchangePair, Reserve>(1)
            .reader(
                RepositoryItemReaderBuilder<ExchangePair>()
                    .name("updateReserveStepReader")
                    .methodName("findAll")
                    .repository(exchangePairRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(
                CompositeItemProcessorBuilder<ExchangePair, Reserve>()
                    .delegates(
                        ItemProcessor<ExchangePair, MsgAddress> { it.address },
                        appendLastBlockIdProcessor,
                        fetchReserveProcessor,
                    )
                    .build()
            )
            .writer(
                RepositoryItemWriterBuilder<Reserve>()
                    .methodName("save")
                    .repository(reserveRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateReserveJob(
        jobRepository: JobRepository,
        updateReserveStep: Step,
    ) = JobBuilder("updateReserveJob")
        .repository(jobRepository)
        .incrementer(RunIdIncrementer())
        .start(updateReserveStep)
        .build()

    @Bean
    fun addExchangePairLpToken2TokenStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        exchangePairTokenRepository: ExchangePairTokenRepository,
        tokenRepository: TokenRepository,
    ) =
        StepBuilder("addExchangePairTokens2TokenStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<ExchangePairToken, Token>(1)
            .reader(
                RepositoryItemReaderBuilder<ExchangePairToken>()
                    .name("addExchangePairTokens2TokenStepReader")
                    .methodName("findAll")
                    .repository(exchangePairTokenRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(Function { exchangePair ->
                // Exchange pairs themselves represent LP token master contracts
                if (tokenRepository.existsByAddress(exchangePair.address)) {
                    null
                } else {
                    Token(
                        address = exchangePair.address,
                        timestamp = Instant.now(),
                    )
                }
            })
            .writer(
                RepositoryItemWriterBuilder<Token>()
                    .methodName("save")
                    .repository(tokenRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun addExchangePairBaseToken2TokenStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        exchangePairTokenRepository: ExchangePairTokenRepository,
        tokenRepository: TokenRepository,
    ) =
        StepBuilder("addExchangePairBaseToken2TokenStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<ExchangePairToken, Token>(1)
            .reader(
                RepositoryItemReaderBuilder<ExchangePairToken>()
                    .name("addExchangePairBaseToken2TokenStepReader")
                    .methodName("findAll")
                    .repository(exchangePairTokenRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(Function { exchangePair ->
                if (tokenRepository.existsByAddress(exchangePair.base)) {
                    null
                } else {
                    Token(
                        address = exchangePair.base,
                        timestamp = Instant.now(),
                    )
                }
            })
            .writer(
                RepositoryItemWriterBuilder<Token>()
                    .methodName("save")
                    .repository(tokenRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun addExchangePairQuoteToken2TokenStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        exchangePairTokenRepository: ExchangePairTokenRepository,
        tokenRepository: TokenRepository,
    ) =
        StepBuilder("addExchangePairQuoteToken2TokenStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<ExchangePairToken, Token>(1)
            .reader(
                RepositoryItemReaderBuilder<ExchangePairToken>()
                    .name("addExchangePairQuoteToken2TokenStepReader")
                    .methodName("findAll")
                    .repository(exchangePairTokenRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(Function { exchangePair ->
                if (tokenRepository.existsByAddress(exchangePair.quote)) {
                    null
                } else {
                    Token(
                        address = exchangePair.quote,
                        timestamp = Instant.now(),
                    )
                }
            })
            .writer(
                RepositoryItemWriterBuilder<Token>()
                    .methodName("save")
                    .repository(tokenRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateTokenContractStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        tokenRepository: TokenRepository,
        appendLastBlockIdProcessor: AppendLastBlockIdProcessor<MsgAddress>,
        fetchTokenContractProcessor: FetchTokenContractProcessor,
        tokenContractRepository: TokenContractRepository,
    ) =
        StepBuilder("updateTokenContractStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<Token, TokenContract>(1)
            .reader(
                RepositoryItemReaderBuilder<Token>()
                    .name("updateTokenContractStepReader")
                    .methodName("findAll")
                    .repository(tokenRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(
                CompositeItemProcessorBuilder<Token, TokenContract>()
                    .delegates(
                        ItemProcessor<Token, MsgAddress> { it.address },
                        appendLastBlockIdProcessor,
                        fetchTokenContractProcessor,
                    )
                    .build()
            )
            .writer(
                RepositoryItemWriterBuilder<TokenContract>()
                    .methodName("save")
                    .repository(tokenContractRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateTokenMetadataStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        tokenContractRepository: TokenContractRepository,
        fetchTokenMetadataProcessor: FetchTokenMetadataProcessor,
        tokenMetadataRepository: TokenMetadataRepository,
    ) =
        StepBuilder("updateTokenMetadataStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<TokenContract, TokenMetadata>(1)
            .reader(
                RepositoryItemReaderBuilder<TokenContract>()
                    .name("updateTokenMetadataStepReader")
                    .methodName("findAll")
                    .repository(tokenContractRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(fetchTokenMetadataProcessor)
            .writer(
                RepositoryItemWriterBuilder<TokenMetadata>()
                    .methodName("save")
                    .repository(tokenMetadataRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateTokenStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        tokenRepository: TokenRepository,
    ) =
        StepBuilder("updateTokenStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<Token, Token>(1)
            .reader(
                RepositoryItemReaderBuilder<Token>()
                    .name("updateTokenStepReader")
                    .methodName("findAll")
                    .repository(tokenRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(
                Function { token ->
                    token.apply {
                        this.timestamp = Instant.now()
                    }
                }
            )
            .writer(
                RepositoryItemWriterBuilder<Token>()
                    .methodName("save")
                    .repository(tokenRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateTokenJob(
        jobRepository: JobRepository,
        taskExecutor: TaskExecutor,
        addExchangePairLpToken2TokenStep: Step,
        addExchangePairBaseToken2TokenStep: Step,
        addExchangePairQuoteToken2TokenStep: Step,
        updateTokenContractStep: Step,
        updateTokenMetadataStep: Step,
        updateTokenStep: Step,
    ) = JobBuilder("updateTokenJob")
        .repository(jobRepository)
        .incrementer(RunIdIncrementer())
        .start(
            FlowBuilder<SimpleFlow>("updateTokenJobFlow")
                .split(taskExecutor)
                .add(
                    FlowBuilder<SimpleFlow>("addExchangePairLpToken2TokenStepFlow")
                        .start(addExchangePairLpToken2TokenStep)
                        .build(),
                    FlowBuilder<SimpleFlow>("addExchangePairBaseToken2TokenStepFlow")
                        .start(addExchangePairBaseToken2TokenStep)
                        .build(),
                    FlowBuilder<SimpleFlow>("addExchangePairQuoteToken2TokenStepFlow")
                        .start(addExchangePairQuoteToken2TokenStep)
                        .build(),
                )
                .build()
        )
        .next(updateTokenContractStep)
        .next(updateTokenMetadataStep)
        .next(updateTokenStep)
        .build()
        .build()

    @Bean
    fun updateLiquidityStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        liquidityRepository: LiquidityRepository,
        appendLastBlockIdProcessor: AppendLastBlockIdProcessor<Liquidity>,
        fetchLiquidityProcessor: FetchLiquidityProcessor,
    ) =
        StepBuilder("updateLiquidityStep")
            .repository(jobRepository)
            .transactionManager(transactionManager)
            .chunk<Liquidity, Liquidity>(1)
            .reader(
                RepositoryItemReaderBuilder<Liquidity>()
                    .name("updateLiquidityStepReader")
                    .methodName("findAll")
                    .repository(liquidityRepository)
                    .sorts(mapOf("timestamp" to Direction.DESC))
                    .build()
            )
            .processor(
                CompositeItemProcessorBuilder<Liquidity, Liquidity>()
                    .delegates(
                        appendLastBlockIdProcessor,
                        fetchLiquidityProcessor,
                    )
                    .build()
            )
            .writer(
                RepositoryItemWriterBuilder<Liquidity>()
                    .methodName("save")
                    .repository(liquidityRepository)
                    .build()
            )
            .allowStartIfComplete(true)
            .build()

    @Bean
    fun updateLiquidityJob(
        jobRepository: JobRepository,
        taskExecutor: TaskExecutor,
        updateLiquidityStep: Step,
    ) = JobBuilder("updateLiquidityJob")
        .repository(jobRepository)
        .incrementer(RunIdIncrementer())
        .start(updateLiquidityStep)
        .build()
}
