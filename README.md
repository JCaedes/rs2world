# RS2#317
<p>A very basic interpretation of a RS2 server of the famous 317 revision with a focus on a region-centered server design.
Region-centered meaning that entity updating will be focussed on regions that are active (active meaning regions that contain players 
or regions neighbouring regions with players). Inactive regions and their contained entities (NPCs, items and objects) will <b>not</b> be updated
and will be serizalized after there are no events relevant to them. When a player enters or nears an inactive region, the region system will check if
there is seriazalized data available for that region. If not, it will construct the region from the data in the cache.</p>

Current checkpoints in development are:
<ul>
<li>RS2#317 Protocol</li>
  <ul>
  <li>Login protocol + serialization</li>
  <li>Game protocol</li>
  </ul>

<li>Cache</li>
  <ul>
  <li>Cache unpacking</li>
  <li>Cache packing</li>
  </ul>

<li>Region based update system</li>
  <ul>
  <li>Constructing game world from cache</li>
  <li>Pathfinding based on data from cache</li>
  <li>Region serizalization</li>
  <li>Active region updating</li>
  </ul>

<li>Concurrency</li>
<li>Remote login server</li>
<li>Database for save data</li>
</ul>
