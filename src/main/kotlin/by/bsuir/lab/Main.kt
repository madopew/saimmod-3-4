package by.bsuir.lab

import kotlin.random.Random

class Task {
    var refusedOnEmitter = false
    var refusedOnChannel = false
    var ticksInQueue = 0
    var ticksInChannelOne = 0
    var ticksInChannelTwo = 0

    val isRefused: Boolean
        get() = refusedOnEmitter || refusedOnChannel

    val ticksInSystem: Int
        get() = ticksInQueue + ticksInChannelOne + ticksInChannelTwo
}

fun Task?.toState() = if (this == null) 0 else 1

fun main() {
    val ticksCount = 1_000_000
    var ticksToEmit = 2
    val stateMap = mutableMapOf<String, Int>()

    val emittedTasks = mutableListOf<Task>()
    var queueTask: Task? = null
    var channelOneTask: Task? = null
    var channelTwoTask: Task? = null

    for (i in 1..ticksCount) {
        val stateName = "$ticksToEmit${queueTask.toState()}${channelOneTask.toState()}${channelTwoTask.toState()}"
        stateMap[stateName] = stateMap.getOrDefault(stateName, 0) + 1

        ticksToEmit--
        var emitted = false
        if (ticksToEmit == 0) {
            ticksToEmit = 2
            emitted = true
        }
        val channelOneFinished = Random.nextDouble() > 0.4
        val channelTwoFinished = Random.nextDouble() > 0.5

        if (channelTwoTask != null) {
            channelTwoTask.ticksInChannelTwo++
            if (channelTwoFinished) {
                channelTwoTask = null
            }
        }

        if (channelOneTask != null) {
            channelOneTask.ticksInChannelOne++
            if (channelOneFinished) {
                if (channelTwoTask == null) {
                    channelTwoTask = channelOneTask
                } else {
                    channelOneTask.refusedOnChannel = true
                }
                channelOneTask = null
            }
        }

        if (queueTask != null) {
            queueTask.ticksInQueue++
            if (channelOneTask == null) {
                channelOneTask = queueTask
                queueTask = null
            }
        }

        if (emitted) {
            val task = Task()
            emittedTasks.add(task)
            if (channelOneTask == null) {
                channelOneTask = task
            } else if (queueTask == null) {
                queueTask = task
            } else {
                task.refusedOnEmitter = true
            }
        }
    }

    println("==================== Состояния ====================")
    for (state in stateMap) {
        println("P_${state.key} = ${state.value.toDouble() / ticksCount}")
    }

    val outputIntensity = emittedTasks.count { !it.isRefused }.toDouble() / ticksCount
    val successProbability = emittedTasks.count { !it.isRefused }.toDouble() / emittedTasks.size
    val refuseProbability = emittedTasks.count { it.isRefused }.toDouble() / emittedTasks.size
    val blockProbability = 0
    val averageQueueLength = emittedTasks.sumOf { it.ticksInQueue }.toDouble() / ticksCount
    val averageSystemLength = emittedTasks.sumOf { it.ticksInSystem }.toDouble() / ticksCount

    val passedEmitter = emittedTasks.filter { !it.refusedOnEmitter }
    val passedChannel = passedEmitter.filter { !it.refusedOnChannel }
    val averageTimeInQueue = emittedTasks.sumOf { it.ticksInQueue }.toDouble() / passedEmitter.size
    val averageTimeInSystem =
            passedEmitter.sumOf { it.ticksInQueue + it.ticksInChannelOne }.toDouble() / passedEmitter.size +
            passedChannel.sumOf { it.ticksInChannelTwo }.toDouble() / passedChannel.size

    val channelOneBusyProbability = emittedTasks.sumOf { it.ticksInChannelOne }.toDouble() / ticksCount
    val channelTwoBusyProbability = emittedTasks.sumOf { it.ticksInChannelTwo }.toDouble() / ticksCount

    println("")
    println("==================== Характеристики ====================")
    println("A = $outputIntensity")
    println("Q = $successProbability")
    println("Pотк = $refuseProbability")
    println("Pбл = $blockProbability")
    println("Lоч = $averageQueueLength")
    println("Lс = $averageSystemLength")
    println("Wоч = $averageTimeInQueue")
    println("Wс = $averageTimeInSystem")
    println("K1 = $channelOneBusyProbability")
    println("K2 = $channelTwoBusyProbability")
}