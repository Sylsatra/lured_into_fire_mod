# 🔥 Lure Mod - Attract Mobs with Blocks & Items! 🔥
Tired of chasing after animals? Let them come to you! This mod allows you to lure specific mobs using custom blocks or items in hand. Watch as sheep gather around a campfire or cows follow you holding wheat!

Mod Showcase:
https://www.youtube-nocookie.com/embed/kLlFetYb830

# Default Config: luremod.toml
By default, horses and sheep will go toward a campfire or a player holding a torch.

# 🛠 How to Configure:

<details>
<summary>Spoiler</summary>
Modify the luremod.toml file to choose:
✅ Mobs that will be attracted (e.g., "minecraft:pig")
✅ Blocks that act as lures (e.g., "minecraft:hay_block")
✅ Items that lure when held (e.g., "minecraft:torch")
✅ Distances like how far mobs detect lures and how close they stop


My config from my modpack Walking Among The Dinosaur:

[luremod]
#List of entity IDs (in "modid:entityid" format) that will be attracted.
#Add or remove entries to pick which mobs get this AI.
#Examples:
#  - "minecraft:pig"
#  - "minecraft:cow"
#  - "someothermod:custommob"
#
entities = ["fossil:meganeura", "fossil:megalograptus", "fossil:aquilolamna", "fossil:coelacanth", "fossil:walliserops", "fossil:sturgeon", "fossil:alligator_gar", "fossil:nautilus", "fossil:mosasaurus", "fossil:megalodon", "fossil:spinosaurus", "fossil:tyrannosaurus"]
#Block IDs (in "modid:blockid" format) that the mob is attracted to.
#Any block in this list will attract the entity if within the search radius.
#Examples:
#  - "minecraft:hay_block"
#  - "minecraft:campfire"
#  - "someothermod:coolblock"
#
blocks = ["minecraft:campfire", "minecraft:fire", "minecraft:torch", "minecraft:glowstone"]
#Any item ("modid:itemid") that attracts the mob if a nearby player holds it.
#Examples:
#  - "minecraft:wheat"
#  - "minecraft:torch"
#  - "someothermod:specialbait"
#
items = ["minecraft:torch", "minecraft:glowstone"]

[luremod.distance]
#How close (in blocks) the mob should get before it stops moving toward the lure.
#If set to 0.5, for example, the mob will move very close to the lure.
stopDistance = 6.0
#Horizontal radius (X/Z in blocks) to scan for lure blocks or players.
#Increasing this can have a performance impact if many entities are active.
searchRadius = 12
#Vertical range (Y in blocks) above/below the mob to scan for blocks/players.
#e.g. "2" means 2 blocks up/down from the mob's position.
verticalRange = 2



</details>







Fully customizable & easy to tweak! 🏗️ Get creative and make mobs follow your lead! 🐑�
