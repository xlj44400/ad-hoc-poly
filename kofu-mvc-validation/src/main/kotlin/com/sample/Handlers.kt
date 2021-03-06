package com.sample

import arrow.core.*
import com.validation.User
import com.validation.ValidationError
import com.validation.rules.validateEmailWithRules
import com.validation.typeclass.errorAccumulation
import com.validation.typeclass.failFast
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.badRequest
import org.springframework.web.servlet.function.ServerResponse.ok
import org.springframework.web.servlet.function.body

class Handlers(
        private val userRepository: UserRepository,
        private val cityRepository: CityRepository
) {
    fun listApi(request: ServerRequest): ServerResponse {
        return ok().contentType(MediaType.APPLICATION_JSON).body(userRepository.findAll())
    }

    fun upsert(request: ServerRequest): ServerResponse { // 👎🏼 This is struck with using FailFast strategy
        val user = request.body<User>()
        val isEmailValid = validateEmail(user.email)
        return isEmailValid.fold(
                { badRequest().body("$user email validation error: $it") },
                {
                    if (cityRepository.doesCityExistsWith(user.city)) {
                        if (userRepository.doesUserExitsWith(user.login)) {
                            userRepository.update(user)
                            ok().body("Updated!! $user")
                        } else {
                            userRepository.insert(user)
                            ok().body("Inserted!! $user")
                        }
                    } else {
                        badRequest().body("City is invalid!! : $user")
                    }
                }
        )
    }

    companion object Utils {
        private fun validateEmail(email: String): Either<ValidationError, String> =
                if (email.contains("@", false)) {
                    if (email.length <= 250) {
                        email.right()
                    } else {
                        ValidationError.MaxLength(250).left()
                    }
                } else {
                    ValidationError.DoesNotContain("@").left()
                }

        private fun validateEmailFailFastX(email: String): Either<NonEmptyList<ValidationError>, Unit> =
                failFast<ValidationError>().run {
                    validateEmailWithRules(email).fix()
                }

        private fun validateEmailErrorAccumulation(email: String): Either<MutableList<ValidationError>, Unit> {
            val errorList = mutableListOf<ValidationError>()
            if (!email.contains("@", false)) {
                errorList.add(ValidationError.DoesNotContain("@"))
            }
            if (email.length > 250) {
                errorList.add(ValidationError.MaxLength(250))
            }
            return if (errorList.isNotEmpty()) errorList.left() else Unit.right()
        }

        private fun validateEmailErrorAccumulationX(email: String): Validated<NonEmptyList<ValidationError>, Unit> =
                errorAccumulation<ValidationError>().run {
                    validateEmailWithRules(email).fix()
                }
    }
}
