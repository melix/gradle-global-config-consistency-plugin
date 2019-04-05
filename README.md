## Configuration consistency plugin

This plugin adds support for resolving _consistent versions of dependencies_ accross different configurations.
Said differently, it makes sure that if you resolve say the `compileClasspath` and `runtimeClasspath` configurations, then common dependencies would have the same version.

This is only a spike, use it at your own risk. There are a couple options:

The first option is to use global consistency, between all configurations of all projects. The plugin **must** be applied on the root project:

```
plugins {
   id 'me.champeau.gradle.config.consistency-base-global'
}
```

The second one is to use _per project_ consistency, in which case you need to apply this plugin on all projects where you want configuration resolution consistency:

```
plugins {
   id 'me.champeau.gradle.config.consistency-base-local'
}
```

Both `base` plugins require you to configure the usage used to resolve the configurations:

```
configurationConsistency {
   usage = 'java-runtime'
}
```

This basically means that we will try to resolve the configurations "as if" we were looking for a runtime usage.

### Convenience plugins for Java

We also provide a couple of support plugins for the Java ecosystem, which configure the default usage for you. All you need to do is to use this for the global consistency:

```
plugins {
   id 'me.champeau.gradle.config.consistency-java-global'
}
```

or this one for project-local consistency:

```
plugins {
   id 'me.champeau.gradle.config.consistency-java-local'
}
```

### Where do we get dependencies from?

This plugin, _by default_, assumes that we get the whole set of dependencies from the _bucket configurations_. Bucket configurations are the ones which are `canBeConsumed=false` and `canBeResolved=false`.
So, for example, the `implementation`, `api` or `runtimeOnly` configurations.
However, it will _not_ use `compile` or `runtime`.

This behavior can be tweaked by changing the `includeConfiguration` predicate on the `configurationConsistency` extension.
