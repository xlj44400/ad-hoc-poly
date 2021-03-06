/* gakshintala created on 3/3/20 */
package com.sample

import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.async.async
import arrow.fx.reactor.extensions.monok.functor.void
import arrow.fx.reactor.k
import arrow.fx.typeclasses.Async
import com.validation.City
import com.validation.User
import com.validation.ValidationError
import com.validation.typeclass.EffectValidator
import com.validation.typeclass.ForErrorAccumulation
import com.validation.typeclass.Repo
import com.validation.typeclass.errorAccumulation
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.r2dbc.r2dbcH2
import org.springframework.fu.kofu.webflux.webFlux

val dataConfig = configuration {
    beans {
        bean<UserRepository>()
        bean<CityRepository>()
        bean<Repo<ForMonoK>> {
            object : Repo<ForMonoK> {
                override fun User.update() = ref<UserRepository>().update(this).k().void()
                override fun User.insert() = ref<UserRepository>().insert(this).k().void()
                override fun User.doesUserLoginExist() = ref<UserRepository>().doesUserExistsWith(login).k().map { it!! }
                override fun User.isUserCityValid() = ref<CityRepository>().doesCityExistsWith(city).k().map { it!! }
            }
        }
        bean<EffectValidator<ForMonoK, ForErrorAccumulation<ValidationError>, ValidationError>> {
            object : EffectValidator<ForMonoK, ForErrorAccumulation<ValidationError>, ValidationError>, Async<ForMonoK> by MonoK.async() {
                override val repo = ref<Repo<ForMonoK>>()
                override val validatorAE = errorAccumulation<ValidationError>()
            }
        }
        bean {
            HandlersX(ref())
        }
    }
    listener<ApplicationReadyEvent> {
        init(ref(), ref(), ref())
    }
    r2dbcH2()
}

val webFlux = configuration {
    webFlux {
        port = if (profiles.contains("test")) 8181 else 8080
        router {
            val handlers = ref<Handlers>()
            val handlersX = ref<HandlersX>()
            POST("/api/upsert", handlersX::upsertX)
            GET("/api/user/all", handlers::listApi)
        }
        codecs {
            string()
            jackson()
        }
    }
}

fun init(client: DatabaseClient,
         userRepository: UserRepository,
         cityRepository: CityRepository
) {
    val createUsers = "CREATE TABLE IF NOT EXISTS users (login varchar PRIMARY KEY, email varchar, first_name varchar, last_name varchar, city varchar);"
    val createCity = "CREATE TABLE IF NOT EXISTS city (name varchar PRIMARY KEY);"
    client.execute(createUsers).then()
            .then(userRepository.deleteAll())
            .then(userRepository.insert(User("smaldini", "smaldini@kt.com", "Stéphane", "Maldini", "london")))
            .then(userRepository.insert(User("sdeleuze", "sdeleuze@kt.com", "Sébastien", "Deleuze", "sydney")))
            .then(userRepository.insert(User("bclozel", "bclozel@kt.com", "Brian", "Clozel", "istanbul")))
            .block()

    client.execute(createCity).then()
            .then(cityRepository.deleteAll())
            .then(cityRepository.insert(City("london")))
            .then(cityRepository.insert(City("sydney")))
            .then(cityRepository.insert(City("istanbul")))
            .block()
}
