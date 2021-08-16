# TO DO 1.17.1 PORT

List of bugs, fixes, and features for the 1.17 version of Extra Golems

## Steps

- X Update mappings
- X Fix initial compile errors
- _ Add entity types: "basic" and "multitexture"
- X Add JSON resource loader
- _ Separate GolemContainer and RenderSettings
- X Add GolemCointainer Codec
  - X Add GolemAttributes and Codec
  - X Add GolemMultitextureSettings and Codec
- _ Add GolemBehavior for all special behavior
  - _ aoe_dry
  - _ aoe_freeze
  - _ arrows
  - _ explode
  - X hurt
  - X attack
  - X place_blocks
  - X passive_effect
  - X split
  - _ teleport
  - X use_fuel
- X Add RenderSettings Codec


## Bugs

- _ Fix dynamic golem rendering to tile block textures
- _ Fix Kitty layer rendering
- _ Fix golem book textures

## New Features

- _ Add "/golem" command (shortcut for "/summon" that is specialized for golems)
- _ Re-add HWYLA integration when available
- _ Make datapacks for other mods (Mekanism, Immersive Engineering, Thermal, CLib, Quark)


## Datapack format

Used to save data that must be synced between server and client through a datapack.

### Example data/golems/golems/clay.json

The below example includes all functionality that will be available for each golem.

```
{
  "material": "clay", // unique id that must match the file name
  "attributes": {
    "health": 20.0,
    "attack": 2.0,
    "speed": 0.25, // movement speed
    "knockback_resistance": 0.4,
    "armor": 0, // damage resistance
    "knockback": 1.0, // attack knockback multiplier
    "immune_to_fire": false,
    "immune_to_explosions": false,
    "hurt_by_water": false, // true if hurt by touching water
    "hurt_by_fall": false,
  },
  "swim_ability": "sink", // "sink", "float", or "swim"
  "glow": 11,
  "power": 15,
  "hidden": false, // true to omit from golem book
  "sound": "minecraft:block.gravel.step",
  "blocks": [
	"minecraft:clay_block",
	"#minecraft:blocks/clay" // example of block tag, doesn't actually exist
  ],
  "heal_items": { // map of <item, percent health restored>
    "minecraft:clay": 0.5,
	"minecraft:clay_ball": 0.1,
	"#minecraft:items/clay" // example of item tag, doesn't actually exist
  }
  // examples of each goal that can be added to any golem
  "goals": { // Map of key-value pairs, can be empty
  
	// To do when attacking
	"attack": {
	  "fire": { // sets the enemy on fire
	    "target": "enemy",
		"chance": 0.9,
		"time": 2 // number of seconds of fire
	  }, 
	  "potion_array": [ // applies a random effect from this array
	    {
  	  	  "Potion": "minecraft:poison",
  	  	  "Amplifier": 0,
  	  	  "Duration": 400
  	    }
	  ],
	  "potion_target": "enemy",
	  "potion_chance": 0.75
	},
	
    // To do when attacked
	"hurt": {
	  "fire": { // sets the enemy on fire
	    "target": "enemy",
		"chance": 0.9,
		"time": 4 // number of seconds of fire
	  }, 
	  "effect" { // applies a potion effect
		"target": "self",
		"chance": 0.25,
		"effects" [ // applies a random effect from this array
	      {
  	  	    "Potion": "minecraft:regeneration",
  	  	    "Amplifier": 0,
  	  	    "Duration": 400
  	      }
	    ],
	  }
	},
	
	// Add potion effect with chance each tick
	"passive_effect": {
	  "night_only": false, // only add potion effect at night or in dimension with no night
	  "effect" { // applies a potion effect
		"target": "self",
		"chance": 0.04,
		"effects" [ // applies a random effect from this array
	      {
  	  	    "Potion": "minecraft:regeneration",
  	  	    "Amplifier": 0,
  	  	    "Duration": 400
  	      }
	    ],
	  }
	}
	
	// Cause explosions
	"explode": {
	  "radius": 1.5,
	  "fuse": 60,
	  "chance_on_hurt": 0.1, // percent chance to apply when hurt
	  "chance_on_attack": 0.02 // percent chance to apply when attacking
	},
	
	// Teleport randomly (or toward enemies)
	"teleport": {
	  "range": 32, // range is 64 for enderman
	  "chance_on_idle": 0.2, // percent chance to apply each tick
	  "chance_on_hurt": 0.8 // percent chance to apply when hurt
	},
	
    // Freeze surrounding blocks
    "aoe_freeze": {
	  "range": 4,
	  "interval": 4
	},
	
	// Dry surrounding water/waterlogged blocks
    "aoe_dry": {
	  "range": 4,
	  "interval": 5
	},
	
	// Store and shoot arrows
	"arrows": {
	  "arrow_damage": 4.25,
	  "interval": 28
	},
	
	// Store and use fuel
	"use_fuel": {
	  "max_fuel": 102400,
	  "burn_interval": 10 // number of ticks before depleting fuel
	},
	
	// Place plants or other blocks
	"place_blocks": {
	  "interval": 30,
	  "blocks": [
	    "minecraft:brown_mushroom",
		"#minecraft:blocks/small_flowers" // Block tag
	  ],
	  "supports": [ // blocks that can have the blocks placed on top of them
	    "minecraft:stone",
		"#minecraft:blocks/dirt" // Block tag
	  ]
	}
	
	// Split into a number of mini golems upon death
	"split": {
	  "children": 2
	}
  }
}
```

#### Multi-textured example data/golems/golems/mushroom.json

The below information will allow multitexture behavior if present. Here is an example for the mushroom golem. 

```
{
  [...],
  "multitexture": {
    "texture_count": 2, // number of textureIDs
    "cycle": true, // true to change texture on player interact
    "block_texture_map": { // map of block->textureID
      "minecraft:mushroom_stem": 0,
      "minecraft:brown_mushroom_block": 0,
      "minecraft:red_mushroom_block": 1
    },
	"loot_table_map": { // map of textureID->loot table
      "0": 0,
      "1": 0
	},
    "texture_glow_map": { // map of textureID->lightLevel; defaults to 0
      "0": 0,
    	"1": 0
    }
  }
}
```

## Rendering

Used to save information about rendering/texturing the golem

### Example assets/golems/golems/clay.json

The below example includes all functionality that will be available for each golem.

```
{
  "material": "clay", // unique id that must match the file name,
  "description": [ // array of text components to add to the golem book
    {
	  "key": "entitytip.translation_key",
	  "style": "§b"
	}
  ],
  "base": "minecraft:clay" // texture path assets/minecraft/textures/block/clay
  // for prefab textures - "base": "#golems:dispenser" // '#' indicates texture path assets/golems/textures/entity/dispenser.png
  "base_template": "golems:layer/mushroom", // template for rendering
  "base_color": 0, // color value to apply
  "use_biome_color": false, // when true, uses biome foliage color instead of base_color
  "base_light": true, // if not present, uses GolemBase#isProvidingLight
  "transparent": false,
  "layers": [ // SimpleTextureLayer
    {
	  "texture": "golems:layer/vines", // texture path "assets/golems/textures/entity/layer/vines.png"
	  "color": 8626266, // colorize vines
	  "light": true // causes vines to glow
	},
    {
	  "texture": "golems:layer/eyes/eyes", // texture path "assets/golems/textures/entity/layer/eyes/eyes.png"
	  "light": true // causes eyes to glow
	},
    {
	  "texture": "golems:layer/gold_edging" // texture path "assets/golems/textures/entity/layer/gold_edging.png"
	}
  ]
  
}
```

### Multi-textured Example assets/golems/golems/furnace.json

The below information will be expected when `"multitexture"` is present. Here is an example for the mushroom golem. 

```
{
  [...],
  "multitexture": {
    "base_map": { // map of textureID->block/prefab texture
      0: "#golems:furnace/lit", // '#' indicates prefab texture
	  1: "#golems:furnace/unlit" // texture path "assets/golems/textures/entity/furnace/unlit.png"
    }
  }
}
```