# Cardinal-Components-API
A components API for Fabric that is easy, modular, and fast.

## Adding the API to your buildscript (loom 0.2.5):
*Note: as no 2.0 version of Cardinal Components API is available on the main maven, this fork offers an alternative using [Jitpack](https://jitpack.io/#Pyrofab/Cardinal-Components-API)*

[![](https://jitpack.io/v/Pyrofab/Cardinal-Components-API.svg)](https://jitpack.io/#Pyrofab/Cardinal-Components-API)

```gradle
repositories {
    maven {
        name = "JitPack"
        url "https://jitpack.io"
    }
}

dependencies {
    // Replace modImplementation with modApi if you expose components in your own API
    modImplementation "com.github.Pyrofab:Cardinal-Components-API:<VERSION>"
}
```
**The include configuration will not work with JitPack and the master jar! See how to include individual modules below.**

You can find the current version of the API in the [releases](https://github.com/Pyrofab/Cardinal-Components-API/releases) tab of the repository on Github.

Cardinal Components API is split into several modules. The main artifact bundles every module, but often all
are not required for a project. To depend on a specific module, use the dependency string
`com.github.Pyrofab.Cardinal-Components-API:<MODULE>:<VERSION>`. Module names can be found below.

Example:
```gradle
// Adds an API dependency on the base cardinal components module
modApi "com.github.Pyrofab.Cardinal-Components-API:cardinal-components-base:<VERSION>"
// Includes the base module as a Jar-in-Jar dependency (optional)
include "com.github.Pyrofab.Cardinal-Components-API:cardinal-components-base:<VERSION>"

```

## Usage

To get started, you only need 2 things: an interface extending `Component`, and a class implementing this interface.

Minimal code example:
```java
interface IntComponent extends Component {
    int getValue();
}

class RandomIntComponent implements IntComponent {
    private int value = (int) (Math.random() * 20);
    @Override public int getValue() { return this.value; }
    @Override public void fromTag(CompoundTag tag) { this.value = tag.getInt("value"); }
    @Override public CompoundTag toTag(CompoundTag tag) { tag.putInt("value", this.value); return tag; }
}
```
All that is left is to actually use that component.

Components are provided by various objects through the `ComponentProvider` interface. 
To interact with those, you need to register your component type, using `ComponentRegistry.registerIfAbsent`;
the resulting `ComponentType` instance is used as a key for component providers.
```java
public static final ComponentType<IntComponent> MAGIK = 
        ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("mymod:magik"), IntComponent.class);

public static void useMagik(ComponentProvider provider) {
    // Retrieve a provided component
    int magik = MAGIK.get(provider).getValue();
    // Or, if the provider is not guaranteed to provide that component:
    int magik = MAGIK.maybeGet(provider).map(IntComponent::getValue).orElse(0);
    // ...
}
```
*Note: a component class can be reused for several component types*

Cardinal Components API offers component provider implementations for a few vanilla types, each in its own module:

### Entities

Components can be added to entities of any type (modded or vanilla) by registering an `EntityComponentCallback`.
Entity components are saved automatically with the entity. Synchronization must be done either manually or with
help of the [`SyncedComponent`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-base/src/main/java/nerdhub/cardinal/components/api/component/extension/SyncedComponent.java) 
and [`EntitySyncedComponent`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-entity/src/main/java/nerdhub/cardinal/components/api/util/sync/EntitySyncedComponent.java) interfaces.
Cardinal Components also provides mechanisms for handling player respawns. By default, components get copied when
players return from the End, but mods can customize that behaviour through [`RespawnCopyStrategy`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-entity/src/main/java/nerdhub/cardinal/components/api/util/RespawnCopyStrategy.java)
and [`PlayerCopyCallback`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-entity/src/main/java/nerdhub/cardinal/components/api/event/PlayerCopyCallback.java)
to copy all or part of the component data.

**Example:**
```java
// Add the component to every instance of PlayerEntity
EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(MAGIK, new RandomIntComponent()));
// Ensure the component's data is copied when keepInventory is enabled (Optional)
EntityComponents.registerRespawnCopyStrat(MAGIK, RespawnCopyStrategy.INVENTORY);
```

*module: cardinal-components-entity*

### Item Stacks

Components can be added to stacks of any item (modded or vanilla) by registering an `ItemComponentCallback`.
Item stack components are saved and synchronized automatically.

**Notes:**
- `ItemStack` equality: stack equality methods `areTagsEqual` and `isEqualIgnoreDamage` are modified to check component equality.
If you have issues when attaching components to item stacks, it usually means you forgot to implement a proper
`equals` check on your component.
- Empty `ItemStack`: empty item stacks never expose any components, no matter what was originally attached to them.

**Example:**
```java
// Add the component to every stack of wasted diamonds
ItemComponentCallback.event(Items.DIAMOND_HOE).register((stack, components) -> components.put(MAGIK, new RandomIntComponent()));
```

*module: cardinal-components-item*

### Worlds

Components can be added to any world by registering a `WorldComponentCallback`.
World components are saved automatically with the world. Synchronization must be done either manually or with
help of the [`SyncedComponent`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-base/src/main/java/nerdhub/cardinal/components/api/component/extension/SyncedComponent.java) 
and [`WorldSyncedComponent`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-world/src/main/java/nerdhub/cardinal/components/api/util/sync/WorldSyncedComponent.java) interfaces.

**Example:**
```java
// Add the component to every world
WorldComponentCallback.EVENT.register((world, components) -> components.put(MAGIK, new RandomIntComponent()));
```

*module: cardinal-components-world*

### Levels

Components can be added to `LevelProperties` objects by registering a `LevelComponentCallback`.
Level properties are shared between every world in a server, making them useful to store global data.
Level components are saved automatically with the global state. Synchronization must be done either manually or with
help of the [`SyncedComponent`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-base/src/main/java/nerdhub/cardinal/components/api/component/extension/SyncedComponent.java) 
and [`LevelSyncedComponent`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-level/src/main/java/nerdhub/cardinal/components/api/util/sync/LevelSyncedComponent.java) interfaces.

**Example:**
```java
// Add the component to level properties
LevelComponentCallback.EVENT.register((levelProperties, components) -> components.put(MAGIK, new RandomIntComponent()));
```

*module: cardinal-components-level*

### Chunks

Components can be added to chunks by registering a `ChunkComponentCallback`.
Chunk components are saved automatically with the chunk. Synchronization must be done either manually or with
help of the [`SyncedComponent`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-base/src/main/java/nerdhub/cardinal/components/api/component/extension/SyncedComponent.java) 
and [`ChunkSyncedComponent`](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/cardinal-components-chunk/src/main/java/nerdhub/cardinal/components/api/util/sync/ChunkSyncedComponent.java) interfaces.

**Notes:**
- `EmptyChunk`: empty chunks never expose any components, no matter what was originally attached to them.
As such, when chunk components are queried on the client, one should make sure the chunk is loaded, or use
`ComponentType#maybeGet` to retrieve a component.

**Example:**
```java
// Add the component to every chunk in every world
ChunkComponentCallback.EVENT.register((chunk, components) -> components.put(MAGIK, new RandomIntComponent()));
```

*module: cardinal-components-chunk*

### Blocks

Blocks actually implement the `BlockComponentProvider` interface instead of the regular `ComponentProvider`.
Custom blocks may re-implement that interface themselves to provide components independently of the presence of
a BlockEntity. Usually the block simply proxies its Block Entity, however the Block Entity does not need to 
implement `BlockComponentProvider` if the block already has a custom implementation. Block components can be
slightly less convenient to provide as they require their own implementations, but [several utility classes](https://github.com/NerdHubMC/Cardinal-Components-API/tree/master/cardinal-components-block/src/main/java/nerdhub/cardinal/components/api/util/sided)
are available to help.

Components are entirely compatible with [LibBlockAttributes](https://github.com/AlexIIL/LibBlockAttributes)' attributes.
Since `Component` is an interface, any attribute instance can easily implement it. Conversely, making an `Attribute`
for an existing `Component` is as simple as calling `Attributes.create(MyComponent.class)`.

*module: cardinal-components-block*


## Test Mod
A test mod for the API is available in this repository, under `src/testmod`. It makes uses of most features from the API.
Its code is outlined in a secondary [readme](https://github.com/NerdHubMC/Cardinal-Components-API/blob/master/src/testmod/readme.md).
