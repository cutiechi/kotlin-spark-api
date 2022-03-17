/*-
 * =LICENSE=
 * Kotlin Spark API: API for Spark 3.2+ (Scala 2.12)
 * ----------
 * Copyright (C) 2019 - 2022 JetBrains
 * ----------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =LICENSEEND=
 */
package org.jetbrains.kotlinx.spark.api

import com.sun.org.apache.xml.internal.serialize.OutputFormat
import org.apache.spark.Partitioner
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.api.java.Optional
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.StateSpec
import org.apache.spark.streaming.api.java.JavaDStream
import org.apache.spark.streaming.api.java.JavaDStreamLike
import org.apache.spark.streaming.api.java.JavaMapWithStateDStream
import org.apache.spark.streaming.api.java.JavaPairDStream
import scala.Tuple2
import scala.Tuple3

//fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.reduceByKey(func: (V, V) -> V): JavaDStream<Arity2<K, V>> =
//    mapToPair(Arity2<K, V>::toTuple)
//        .reduceByKey(func)
//        .map(Tuple2<K, V>::toArity)


@JvmName("tuple2ToPairDStream")
fun <K, V> JavaDStreamLike<Tuple2<K, V>, *, *>.toPairDStream(): JavaPairDStream<K, V> =
    mapToPair { it }

@JvmName("arity2ToPairDStream")
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.toPairDStream(): JavaPairDStream<K, V> =
    mapToPair(Arity2<K, V>::toTuple)

@JvmName("pairToPairDStream")
fun <K, V> JavaDStreamLike<Pair<K, V>, *, *>.toPairDStream(): JavaPairDStream<K, V> =
    mapToPair(Pair<K, V>::toTuple)

/**
 * Return a new DStream by applying `groupByKey` to each RDD. Hash partitioning is used to
 * generate the RDDs with Spark's default number of partitions.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.groupByKey(): JavaDStream<Arity2<K, Iterable<V>>> =
    mapToPair { it.toTuple() }
        .groupByKey()
        .map { it.toArity() }

/**
 * Return a new DStream by applying `groupByKey` to each RDD. Hash partitioning is used to
 * generate the RDDs with `numPartitions` partitions.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.groupByKey(numPartitions: Int): JavaDStream<Arity2<K, Iterable<V>>> =
    mapToPair { it.toTuple() }
        .groupByKey(numPartitions)
        .map { it.toArity() }

/**
 * Return a new DStream by applying `groupByKey` on each RDD. The supplied
 * org.apache.spark.Partitioner is used to control the partitioning of each RDD.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.groupByKey(partitioner: Partitioner): JavaDStream<Arity2<K, Iterable<V>>> =
    mapToPair { it.toTuple() }
        .groupByKey(partitioner)
        .map { it.toArity() }

/**
 * Return a new DStream by applying `reduceByKey` to each RDD. The values for each key are
 * merged using the associative and commutative reduce function. Hash partitioning is used to
 * generate the RDDs with Spark's default number of partitions.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.reduceByKey(reduceFunc: (V, V) -> V): JavaDStream<Arity2<K, V>> =
    mapToPair { it.toTuple() }
        .reduceByKey(reduceFunc)
        .map { it.toArity() }

/**
 * Return a new DStream by applying `reduceByKey` to each RDD. The values for each key are
 * merged using the supplied reduce function. Hash partitioning is used to generate the RDDs
 * with `numPartitions` partitions.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.reduceByKey(
    reduceFunc: (V, V) -> V,
    numPartitions: Int,
): JavaDStream<Arity2<K, V>> =
    mapToPair { it.toTuple() }
        .reduceByKey(reduceFunc, numPartitions)
        .map { it.toArity() }

/**
 * Return a new DStream by applying `reduceByKey` to each RDD. The values for each key are
 * merged using the supplied reduce function. org.apache.spark.Partitioner is used to control
 * the partitioning of each RDD.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.reduceByKey(
    reduceFunc: (V, V) -> V,
    partitioner: Partitioner,
): JavaDStream<Arity2<K, V>> =
    mapToPair { it.toTuple() }
        .reduceByKey(reduceFunc, partitioner)
        .map { it.toArity() }

/**
 * Combine elements of each key in DStream's RDDs using custom functions. This is similar to the
 * combineByKey for RDDs. Please refer to combineByKey in
 * org.apache.spark.rdd.PairRDDFunctions in the Spark core documentation for more information.
 */
fun <K, V, C> JavaDStreamLike<Arity2<K, V>, *, *>.combineByKey(
    createCombiner: (V) -> C,
    mergeValue: (C, V) -> C,
    mergeCombiner: (C, C) -> C,
    partitioner: Partitioner,
    mapSideCombine: Boolean = true,
): JavaDStream<Arity2<K, C>> =
    mapToPair { it.toTuple() }
        .combineByKey(createCombiner, mergeValue, mergeCombiner, partitioner, mapSideCombine)
        .map { it.toArity() }

/**
 * Return a new DStream by applying `groupByKey` over a sliding window on `this` DStream.
 * Similar to `DStream.groupByKey()`, but applies it over a sliding window.
 * Hash partitioning is used to generate the RDDs with `numPartitions` partitions.
 * @param windowDuration width of the window; must be a multiple of this DStream's
 *                       batching interval
 * @param slideDuration  sliding interval of the window (i.e., the interval after which
 *                       the new DStream will generate RDDs); must be a multiple of this
 *                       DStream's batching interval
 * @param numPartitions  number of partitions of each RDD in the new DStream; if not specified
 *                       then Spark's default number of partitions will be used
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.groupByKeyAndWindow(
    windowDuration: Duration,
    slideDuration: Duration = dstream().slideDuration(),
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
): JavaDStream<Arity2<K, Iterable<V>>> =
    mapToPair { it.toTuple() }
        .groupByKeyAndWindow(windowDuration, slideDuration, numPartitions)
        .map { it.toArity() }

/**
 * Create a new DStream by applying `groupByKey` over a sliding window on `this` DStream.
 * Similar to `DStream.groupByKey()`, but applies it over a sliding window.
 * @param windowDuration width of the window; must be a multiple of this DStream's
 *                       batching interval
 * @param slideDuration  sliding interval of the window (i.e., the interval after which
 *                       the new DStream will generate RDDs); must be a multiple of this
 *                       DStream's batching interval
 * @param partitioner    partitioner for controlling the partitioning of each RDD in the new
 *                       DStream.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.groupByKeyAndWindow(
    windowDuration: Duration,
    slideDuration: Duration = dstream().slideDuration(),
    partitioner: Partitioner,
): JavaDStream<Arity2<K, Iterable<V>>> =
    mapToPair { it.toTuple() }
        .groupByKeyAndWindow(windowDuration, slideDuration, partitioner)
        .map { it.toArity() }

/**
 * Return a new DStream by applying `reduceByKey` over a sliding window. This is similar to
 * `DStream.reduceByKey()` but applies it over a sliding window. Hash partitioning is used to
 * generate the RDDs with `numPartitions` partitions.
 * @param reduceFunc associative and commutative reduce function
 * @param windowDuration width of the window; must be a multiple of this DStream's
 *                       batching interval
 * @param slideDuration  sliding interval of the window (i.e., the interval after which
 *                       the new DStream will generate RDDs); must be a multiple of this
 *                       DStream's batching interval
 * @param numPartitions  number of partitions of each RDD in the new DStream.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.reduceByKeyAndWindow(
    reduceFunc: (V, V) -> V,
    windowDuration: Duration,
    slideDuration: Duration = dstream().slideDuration(),
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
): JavaDStream<Arity2<K, V>> =
    mapToPair { it.toTuple() }
        .reduceByKeyAndWindow(reduceFunc, windowDuration, slideDuration, numPartitions)
        .map { it.toArity() }

/**
 * Return a new DStream by applying `reduceByKey` over a sliding window. Similar to
 * `DStream.reduceByKey()`, but applies it over a sliding window.
 * @param reduceFunc associative and commutative reduce function
 * @param windowDuration width of the window; must be a multiple of this DStream's
 *                       batching interval
 * @param slideDuration  sliding interval of the window (i.e., the interval after which
 *                       the new DStream will generate RDDs); must be a multiple of this
 *                       DStream's batching interval
 * @param partitioner    partitioner for controlling the partitioning of each RDD
 *                       in the new DStream.
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.reduceByKeyAndWindow(
    reduceFunc: (V, V) -> V,
    windowDuration: Duration,
    slideDuration: Duration = dstream().slideDuration(),
    partitioner: Partitioner,
): JavaDStream<Arity2<K, V>> =
    mapToPair { it.toTuple() }
        .reduceByKeyAndWindow(reduceFunc, windowDuration, slideDuration, partitioner)
        .map { it.toArity() }

/**
 * Return a new DStream by applying incremental `reduceByKey` over a sliding window.
 * The reduced value of over a new window is calculated using the old window's reduced value :
 *  1. reduce the new values that entered the window (e.g., adding new counts)
 *
 *  2. "inverse reduce" the old values that left the window (e.g., subtracting old counts)
 *
 * This is more efficient than reduceByKeyAndWindow without "inverse reduce" function.
 * However, it is applicable to only "invertible reduce functions".
 * Hash partitioning is used to generate the RDDs with Spark's default number of partitions.
 * @param reduceFunc associative and commutative reduce function
 * @param invReduceFunc inverse reduce function; such that for all y, invertible x:
 *                      `invReduceFunc(reduceFunc(x, y), x) = y`
 * @param windowDuration width of the window; must be a multiple of this DStream's
 *                       batching interval
 * @param slideDuration  sliding interval of the window (i.e., the interval after which
 *                       the new DStream will generate RDDs); must be a multiple of this
 *                       DStream's batching interval
 * @param filterFunc     Optional function to filter expired key-value pairs;
 *                       only pairs that satisfy the function are retained
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.reduceByKeyAndWindow(
    reduceFunc: (V, V) -> V,
    invReduceFunc: (V, V) -> V,
    windowDuration: Duration,
    slideDuration: Duration = dstream().slideDuration(),
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
    filterFunc: ((Arity2<K, V>) -> Boolean)? = null,
): JavaDStream<Arity2<K, V>> =
    mapToPair { it.toTuple() }
        .reduceByKeyAndWindow(
            reduceFunc,
            invReduceFunc,
            windowDuration,
            slideDuration,
            numPartitions,
            filterFunc?.let {
                { tuple ->
                    filterFunc(tuple.toArity())
                }
            }
        )
        .map { it.toArity() }

/**
 * Return a new DStream by applying incremental `reduceByKey` over a sliding window.
 * The reduced value of over a new window is calculated using the old window's reduced value :
 *  1. reduce the new values that entered the window (e.g., adding new counts)
 *  2. "inverse reduce" the old values that left the window (e.g., subtracting old counts)
 * This is more efficient than reduceByKeyAndWindow without "inverse reduce" function.
 * However, it is applicable to only "invertible reduce functions".
 * @param reduceFunc     associative and commutative reduce function
 * @param invReduceFunc  inverse reduce function
 * @param windowDuration width of the window; must be a multiple of this DStream's
 *                       batching interval
 * @param slideDuration  sliding interval of the window (i.e., the interval after which
 *                       the new DStream will generate RDDs); must be a multiple of this
 *                       DStream's batching interval
 * @param partitioner    partitioner for controlling the partitioning of each RDD in the new
 *                       DStream.
 * @param filterFunc     Optional function to filter expired key-value pairs;
 *                       only pairs that satisfy the function are retained
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.reduceByKeyAndWindow(
    reduceFunc: (V, V) -> V,
    invReduceFunc: (V, V) -> V,
    windowDuration: Duration,
    slideDuration: Duration = dstream().slideDuration(),
    partitioner: Partitioner,
    filterFunc: ((Arity2<K, V>) -> Boolean)? = null,
): JavaDStream<Arity2<K, V>> =
    mapToPair { it.toTuple() }
        .reduceByKeyAndWindow(
            reduceFunc,
            invReduceFunc,
            windowDuration,
            slideDuration,
            partitioner,
            filterFunc?.let {
                { tuple ->
                    filterFunc(tuple.toArity())
                }
            }
        )
        .map { it.toArity() }

/**
 * Return a [MapWithStateDStream] by applying a function to every key-value element of
 * `this` stream, while maintaining some state data for each unique key. The mapping function
 * and other specification (e.g. partitioners, timeouts, initial state data, etc.) of this
 * transformation can be specified using `StateSpec` class. The state data is accessible in
 * as a parameter of type `State` in the mapping function.
 *
 * Example of using `mapWithState`:
 * {{{
 *    // A mapping function that maintains an integer state and return a String
 *    def mappingFunction(key: String, value: Option[Int], state: State[Int]): Option[String] = {
 *      // Use state.exists(), state.get(), state.update() and state.remove()
 *      // to manage state, and return the necessary string
 *    }
 *
 *    val spec = StateSpec.function(mappingFunction).numPartitions(10)
 *
 *    val mapWithStateDStream = keyValueDStream.mapWithState[StateType, MappedType](spec)
 * }}}
 *
 * @param spec          Specification of this transformation
 * @tparam StateType    Class type of the state data
 * @tparam MappedType   Class type of the mapped data
 */
fun <K, V, StateType, MappedType> JavaDStreamLike<Arity2<K, V>, *, *>.mapWithState(
    spec: StateSpec<K, V, StateType, MappedType>,
): JavaMapWithStateDStream<K, V, StateType, MappedType> =
    mapToPair { it.toTuple() }
        .mapWithState(spec)

/**
 * Return a new "state" DStream where the state for each key is updated by applying
 * the given function on the previous state of the key and the new values of each key.
 * In every batch the updateFunc will be called for each state even if there are no new values.
 * Hash partitioning is used to generate the RDDs with Spark's default number of partitions.
 * @param updateFunc State update function. If `this` function returns None, then
 *                   corresponding state key-value pair will be eliminated.
 * @tparam S State type
 */
fun <K, V, S> JavaDStreamLike<Arity2<K, V>, *, *>.updateStateByKey(
    updateFunc: (List<V>, S?) -> S?,
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
): JavaDStream<Arity2<K, S>> =
    mapToPair { it.toTuple() }
        .updateStateByKey(
            { list: List<V>, s: Optional<S> ->
                updateFunc(list, s.toNullable()).toOptional()
            },
            numPartitions,
        )
        .map { it.toArity() }

/**
 * Return a new "state" DStream where the state for each key is updated by applying
 * the given function on the previous state of the key and the new values of each key.
 * In every batch the updateFunc will be called for each state even if there are no new values.
 * [[org.apache.spark.Partitioner]] is used to control the partitioning of each RDD.
 * @param updateFunc State update function. Note, that this function may generate a different
 *                   tuple with a different key than the input key. Therefore keys may be removed
 *                   or added in this way. It is up to the developer to decide whether to
 *                   remember the partitioner despite the key being changed.
 * @param partitioner Partitioner for controlling the partitioning of each RDD in the new
 *                    DStream
 * @tparam S State type
 */
fun <K, V, S> JavaDStreamLike<Arity2<K, V>, *, *>.updateStateByKey(
    updateFunc: (List<V>, S?) -> S?,
    partitioner: Partitioner,
): JavaDStream<Arity2<K, S>> =
    mapToPair { it.toTuple() }
        .updateStateByKey(
            { list: List<V>, s: Optional<S> ->
                updateFunc(list, s.toNullable()).toOptional()
            },
            partitioner,
        )
        .map { it.toArity() }

/**
 * Return a new "state" DStream where the state for each key is updated by applying
 * the given function on the previous state of the key and the new values of the key.
 * org.apache.spark.Partitioner is used to control the partitioning of each RDD.
 * @param updateFunc State update function. If `this` function returns None, then
 *                   corresponding state key-value pair will be eliminated.
 * @param partitioner Partitioner for controlling the partitioning of each RDD in the new
 *                    DStream.
 * @param initialRDD initial state value of each key.
 * @tparam S State type
 */
fun <K, V, S> JavaDStreamLike<Arity2<K, V>, *, *>.updateStateByKey(
    updateFunc: (List<V>, S?) -> S?,
    partitioner: Partitioner,
    initialRDD: JavaRDD<Arity2<K, S>>,
): JavaDStream<Arity2<K, S>> =
    mapToPair { it.toTuple() }
        .updateStateByKey(
            { list: List<V>, s: Optional<S> ->
                updateFunc(list, s.toNullable()).toOptional()
            },
            partitioner,
            initialRDD.mapToPair { it.toTuple() },
        )
        .map { it.toArity() }

/**
 * Return a new DStream by applying a map function to the value of each key-value pairs in
 * 'this' DStream without changing the key.
 */
fun <K, V, U> JavaDStreamLike<Arity2<K, V>, *, *>.mapValues(
    mapValuesFunc: (V) -> U,
): JavaDStream<Arity2<K, U>> =
    mapToPair { it.toTuple() }
        .mapValues(mapValuesFunc)
        .map { it.toArity() }

/**
 * Return a new DStream by applying a flatmap function to the value of each key-value pairs in
 * 'this' DStream without changing the key.
 */
fun <K, V, U> JavaDStreamLike<Arity2<K, V>, *, *>.flatMapValues(
    flatMapValuesFunc: (V) -> Iterator<U>,
): JavaDStream<Arity2<K, U>> =
    mapToPair { it.toTuple() }
        .flatMapValues(flatMapValuesFunc)
        .map { it.toArity() }

/**
 * Return a new DStream by applying 'cogroup' between RDDs of `this` DStream and `other` DStream.
 * Hash partitioning is used to generate the RDDs with `numPartitions` partitions.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.cogroup(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
): JavaDStream<Arity2<K, Arity2<Iterable<V>, Iterable<W>>>> =
    mapToPair { it.toTuple() }
        .cogroup(
            other.mapToPair { it.toTuple() },
            numPartitions,
        )
        .map {
            c(it._1, it._2.toArity())
        }

/**
 * Return a new DStream by applying 'cogroup' between RDDs of `this` DStream and `other` DStream.
 * The supplied org.apache.spark.Partitioner is used to partition the generated RDDs.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.cogroup(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    partitioner: Partitioner,
): JavaDStream<Arity2<K, Arity2<Iterable<V>, Iterable<W>>>> =
    mapToPair { it.toTuple() }
        .cogroup(
            other.mapToPair { it.toTuple() },
            partitioner,
        )
        .map {
            c(it._1, it._2.toArity())
        }

/**
 * Return a new DStream by applying 'join' between RDDs of `this` DStream and `other` DStream.
 * Hash partitioning is used to generate the RDDs with `numPartitions` partitions.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.join(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
): JavaDStream<Arity2<K, Arity2<V, W>>> =
    mapToPair { it.toTuple() }
        .join(
            other.mapToPair { it.toTuple() },
            numPartitions,
        )
        .map {
            c(it._1, it._2.toArity())
        }

/**
 * Return a new DStream by applying 'join' between RDDs of `this` DStream and `other` DStream.
 * The supplied org.apache.spark.Partitioner is used to control the partitioning of each RDD.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.join(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    partitioner: Partitioner,
): JavaDStream<Arity2<K, Arity2<V, W>>> =
    mapToPair { it.toTuple() }
        .join(
            other.mapToPair { it.toTuple() },
            partitioner,
        )
        .map {
            c(it._1, it._2.toArity())
        }

/**
 * Return a new DStream by applying 'left outer join' between RDDs of `this` DStream and
 * `other` DStream. Hash partitioning is used to generate the RDDs with `numPartitions`
 * partitions.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.leftOuterJoin(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
): JavaDStream<Arity2<K, Arity2<V, W?>>> =
    mapToPair { it.toTuple() }
        .leftOuterJoin(
            other.mapToPair { it.toTuple() },
            numPartitions,
        )
        .map {
            c(it._1, c(it._2._1, it._2._2.toNullable()))
        }

/**
 * Return a new DStream by applying 'left outer join' between RDDs of `this` DStream and
 * `other` DStream. The supplied org.apache.spark.Partitioner is used to control
 * the partitioning of each RDD.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.leftOuterJoin(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    partitioner: Partitioner,
): JavaDStream<Arity2<K, Arity2<V, W?>>> =
    mapToPair { it.toTuple() }
        .leftOuterJoin(
            other.mapToPair { it.toTuple() },
            partitioner,
        )
        .map {
            c(it._1, c(it._2._1, it._2._2.toNullable()))
        }

/**
 * Return a new DStream by applying 'right outer join' between RDDs of `this` DStream and
 * `other` DStream. Hash partitioning is used to generate the RDDs with `numPartitions`
 * partitions.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.rightOuterJoin(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
): JavaDStream<Arity2<K, Arity2<V?, W>>> =
    mapToPair { it.toTuple() }
        .rightOuterJoin(
            other.mapToPair { it.toTuple() },
            numPartitions,
        )
        .map {
            c(it._1, c(it._2._1.toNullable(), it._2._2))
        }

/**
 * Return a new DStream by applying 'right outer join' between RDDs of `this` DStream and
 * `other` DStream. The supplied org.apache.spark.Partitioner is used to control
 * the partitioning of each RDD.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.rightOuterJoin(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    partitioner: Partitioner,
): JavaDStream<Arity2<K, Arity2<V?, W>>> =
    mapToPair { it.toTuple() }
        .rightOuterJoin(
            other.mapToPair { it.toTuple() },
            partitioner,
        )
        .map {
            c(it._1, c(it._2._1.toNullable(), it._2._2))
        }

/**
 * Return a new DStream by applying 'full outer join' between RDDs of `this` DStream and
 * `other` DStream. Hash partitioning is used to generate the RDDs with `numPartitions`
 * partitions.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.fullOuterJoin(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    numPartitions: Int = dstream().ssc().sc().defaultParallelism(),
): JavaDStream<Arity2<K, Arity2<V?, W?>>> =
    mapToPair { it.toTuple() }
        .fullOuterJoin(
            other.mapToPair { it.toTuple() },
            numPartitions,
        )
        .map {
            c(it._1, c(it._2._1.toNullable(), it._2._2.toNullable()))
        }

/**
 * Return a new DStream by applying 'full outer join' between RDDs of `this` DStream and
 * `other` DStream. The supplied org.apache.spark.Partitioner is used to control
 * the partitioning of each RDD.
 */
fun <K, V, W> JavaDStreamLike<Arity2<K, V>, *, *>.fullOuterJoin(
    other: JavaDStreamLike<Arity2<K, W>, *, *>,
    partitioner: Partitioner,
): JavaDStream<Arity2<K, Arity2<V?, W?>>> =
    mapToPair { it.toTuple() }
        .fullOuterJoin(
            other.mapToPair { it.toTuple() },
            partitioner,
        )
        .map {
            c(it._1, c(it._2._1.toNullable(), it._2._2.toNullable()))
        }

/**
 * Save each RDD in `this` DStream as a Hadoop file. The file name at each batch interval is
 * generated based on `prefix` and `suffix`: "prefix-TIME_IN_MS.suffix".
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.saveAsHadoopFiles(
    prefix: String, suffix: String,
): Unit =
    mapToPair { it.toTuple() }
        .saveAsHadoopFiles(prefix, suffix)

/**
 * Save each RDD in `this` DStream as a Hadoop file. The file name at each batch interval is
 * generated based on `prefix` and `suffix`: "prefix-TIME_IN_MS.suffix".
 */
fun <K, V> JavaDStreamLike<Arity2<K, V>, *, *>.saveAsNewAPIHadoopFiles(
    prefix: String, suffix: String,
): Unit =
    mapToPair { it.toTuple() }
        .saveAsNewAPIHadoopFiles(prefix, suffix)