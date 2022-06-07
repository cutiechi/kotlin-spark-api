package org.jetbrains.kotlinx.spark.api

import org.apache.spark.sql.*
import org.apache.spark.sql.types.DataType
import scala.collection.mutable.WrappedArray
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import org.apache.spark.sql.expressions.UserDefinedFunction as SparkUserDefinedFunction

/** Unwraps [DataTypeWithClass]. */
fun DataType.unWrap(): DataType =
    when (this) {
        is DataTypeWithClass -> DataType.fromJson(dt().json())
        else -> this
    }

/**
 * Checks if [this] is of a valid type for a UDF, otherwise it throws a [TypeOfUDFParameterNotSupportedException]
 */
@PublishedApi
internal fun KClass<*>.checkForValidType(parameterName: String) {
    if (this == String::class || isSubclassOf(WrappedArray::class))
        return // Most of the time we need strings or WrappedArrays

    if (isSubclassOf(Iterable::class)
        || java.isArray
        || isSubclassOf(Map::class)
        || isSubclassOf(Array::class)
        || isSubclassOf(ByteArray::class)
        || isSubclassOf(CharArray::class)
        || isSubclassOf(ShortArray::class)
        || isSubclassOf(IntArray::class)
        || isSubclassOf(LongArray::class)
        || isSubclassOf(FloatArray::class)
        || isSubclassOf(DoubleArray::class)
        || isSubclassOf(BooleanArray::class)
    ) throw TypeOfUDFParameterNotSupportedException(this, parameterName)
}

/**
 * An exception thrown when the UDF is generated with illegal types for the parameters
 */
class TypeOfUDFParameterNotSupportedException(kClass: KClass<*>, parameterName: String) : IllegalArgumentException(
    "Parameter $parameterName is subclass of ${kClass.qualifiedName}. If you need to process an array use ${WrappedArray::class.qualifiedName}."
)

/**
 * Registers a user-defined function (UDF) with name, for a UDF that's already defined using the Dataset
 * API (i.e. of type [NamedUserDefinedFunction]).
 * @see UDFRegistration.register
 */
inline fun <RETURN, reified NAMED_UDF : NamedUserDefinedFunction<RETURN, *>> UDFRegistration.register(
    namedUdf: NAMED_UDF,
): NAMED_UDF =
    namedUdf.copy(udf = register(namedUdf.name, namedUdf.udf))

inline fun <RETURN, reified NAMED_UDF : NamedUserDefinedFunction<RETURN, *>> UDFRegistration.register(
    name: String,
    udf: UserDefinedFunction<RETURN, NAMED_UDF>,
): NAMED_UDF =
    udf.withName(name).copy(udf = register(name, udf.udf))

/**
 * Typed wrapper around [SparkUserDefinedFunction] with defined encoder.
 *
 * @param RETURN the return type of the udf
 * @param NAMED a type reference to the named version of the [SparkUserDefinedFunction] implementing class
 */
sealed interface UserDefinedFunction<RETURN, NAMED> {
    val udf: SparkUserDefinedFunction
    val encoder: Encoder<RETURN>

    /** Returns true when the UDF can return a nullable value. */
    val nullable: Boolean get() = udf.nullable()

    /** Returns true iff the UDF is deterministic, i.e. the UDF produces the same output given the same input. */
    val deterministic: Boolean get() = udf.deterministic()

    fun invokeUntyped(vararg params: Column): Column = udf.apply(*params)

    operator fun invoke(vararg params: Column): TypedColumn<*, RETURN> = invokeUntyped(*params).`as`(encoder)

    /** Converts this [UserDefinedFunction] to a [NamedUserDefinedFunction]. */
    fun withName(name: String): NAMED

    /**
     * Converts this [UserDefinedFunction] to a [NamedUserDefinedFunction].
     * @see withName
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): NAMED
}

/**
 * Typed and named wrapper around [SparkUserDefinedFunction] with defined encoder.
 *
 * @param RETURN    the return type of the udf
 * @param THIS      a self reference to the named version of the [SparkUserDefinedFunction] implementing class.
 *                  Unfortunately needed to allow functions to treat any [NamedTypedUserDefinedFunction] as a normal [TypedUserDefinedFunction].
 */
sealed interface NamedUserDefinedFunction<RETURN, THIS> : UserDefinedFunction<RETURN, THIS> {
    val name: String
}

/** Copy method for all [NamedUserDefinedFunction] functions. */
inline fun <R, reified T : NamedUserDefinedFunction<R, *>> T.copy(
    name: String = this.name,
    udf: SparkUserDefinedFunction = this.udf,
    encoder: Encoder<R> = this.encoder,
): T = T::class.primaryConstructor!!.run {
    callBy(
        parameters.associateWith {
            when (it.name) {
                NamedUserDefinedFunction<*, *>::name.name -> name
                NamedUserDefinedFunction<*, *>::udf.name -> udf
                NamedUserDefinedFunction<*, *>::encoder.name -> encoder
                else -> error("Wrong arguments")
            }
        }
    )
}