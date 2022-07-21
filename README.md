# Mirai-Rate-Limiter-Core

扩展 mirai DSL， 提供用于限流的的监听器定义

## 简要介绍

该库提供了一个基于令牌桶算法实现的方法限流器。以令牌桶算法实现的限流器可以提供以稳定速率处理请求的限流服务，同时，通过设定合理的缓存大小，可以在一定程度上支持突然并发。

> 令牌桶算法以固定速率生成令牌，每个被处理的请求需要先获取令牌。当无法获取令牌时，请求会被挂起或者降级返回，以达到限流的目的。
>
> 因为令牌桶可以缓存一定数量的令牌，因此针对缓存令牌数量内的请求，可以进行并发处理。而如果缓存设置为0，则可以以和令牌生成速率相同的速率处理请求。可以说相当灵活。

## 使用方法

### 引入依赖

```kts
dependencies {
    implementation("cn.ryoii:mirai-rate-limit-core:$version")
}
```

### 注册限流器

```kotlin
// 注册无缓存，以平均 60 请求/分钟进行限流，即 1 QPS
val limiter = limitWith(maxCache = 0, limitPerMinute = 60)

// 注册缓存大小为60，以平均 60 请求/分钟进行限流
// 因为存在缓存，最大可以支持 60 QPS 并发
// 但受限于令牌生成速率，1分钟内也只能处理 120 个请求
val limiter = limitWith(maxCache = 60, limitPerMinute = 60)
```

### 将限流器绑定到监听器上

```kotlin
bot.eventChannel.subscribeMessages {
    val limiter = limitWith(maxCache = 1, limitPerMinute = 60)
    
    limit(case("ping"), limiter) {
        subject.sendMessage("pong")
    }
    
    // 也支持绑定到多种 mirai 提供的监听器类型
    limit(snetBy(123456), limiter) {
        subject.sendMessage("Hi" + At(sender.id))
    }
}
```

### 限流器降级

> 默认情况下被限制的请求会挂起等待获取令牌。当限制速率低，但并发量大的时候，大量挂起的请求的增加服务器负担。因此，提供降级处理十分必要。
> 
> 限流器通过提供特定的 fallback 回调处理服务降级。当当前请求获取令牌失败时，不会被挂起，而是直接触发 fallback。
> 
> 当然，如果实在需要丢弃这部分请求，则提供一个空的 fallback 而不是 null。否则请求会被挂起。

```kotlin
bot.eventChannel.subscribeMessages {
    val limiter = limitWith(maxCache = 0, limitPerMinute = 60)
    
    limit(case("ping"), limiter, fallback = {
        subject.sendMessage("Limited...")
    }) {
        subject.sendMessage("pong")
    }
}
```

### 更多完整案例
+ [1 qps 限流器](./src/test/kotlin/example/OncePerSecond.kt)
+ [带缓存限流器](./src/test/kotlin/example/CacheLimit.kt)
+ [降级限流器](./src/test/kotlin/example/LimitWithFallback.kt)
+ [限流器分组](./src/test/kotlin/example/LimitGroup.kt)
+ [限流器分组限流](./src/test/kotlin/example/CustomFallback.kt)

> 更多详情查看源码上文档