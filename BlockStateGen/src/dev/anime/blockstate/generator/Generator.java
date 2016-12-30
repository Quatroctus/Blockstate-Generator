package dev.anime.blockstate.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.Cartesian;

public class Generator {
	
	/**
	 * Generates a Blockstate JSON for the specified block using all of it's properties.
	 * @param path Starting path ie "E://Modding/forge".
	 * @param modid The modid for this blocks mod, used mainly as an identifier.
	 * @param fileName Pretty self explanatory.
	 * @param block The block that is relevant to the blockstate.
	 * @deprecated Use genVanillaBlockState or genForgeBlockState instead of this.
	 */
	@Deprecated
	public static void createJson(String path, String modid, String fileName, Block block) {
		File json = new File(path + "/assets/" + modid + "/blockstates/" + fileName + ".json");
		File folders = new File(path + "/assets/" + modid + "/blockstates/");
		if (json.exists()) return;
			folders.mkdirs();
			try {
				json.createNewFile();
				if (!json.canWrite()) json.setWritable(true);
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				JsonObject obj = new JsonObject();
				JsonObject element = new JsonObject();
				Collection<IBlockState> collec = block.getBlockState().getValidStates();
				for (IBlockState state : collec) {
					Collection<IProperty<?>> properties = state.getPropertyNames();
					String[] names = new String[properties.size()], values = new String[properties.size()];
					for (int i = 0; i < names.length; i++) {
						IProperty<?> prop = (IProperty<?>) properties.toArray()[i];
						names[i] = prop.getName();
						values[i] = state.getProperties().get(prop).toString();
					}
					StringBuilder tagBuilder = new StringBuilder();
					for (int i = 0; i < properties.size(); i++) {
						if (i == properties.size() - 1) {
							tagBuilder.append(names[i] + "=" + values[i]);
						} else tagBuilder.append(names[i] + "=" + values[i] + ",");
					}

					element.add(tagBuilder.toString(), new JsonObject());
				}
				obj.add("variants", element);
				String[] jsonText = gson.toJson(obj).split("[{]");
				for (int i = 0; i < jsonText.length; i++) {
					if (!jsonText[i].endsWith("}")) jsonText[i] = jsonText[i] += "{";
				}
				List<String[]> linesList = new ArrayList<String[]>();
				for (String line : jsonText) {
					String[] array = line.split("},");
					for (int i = 0; i < array.length; i++) {
						if (i != array.length - 1) array[i] = array[i] += "},";
					}
					linesList.add(array);
				}
				BufferedWriter writer = new BufferedWriter(new FileWriter(json));
				for (String[] lines : linesList) {
					for (String line : lines) {
						if (line.startsWith("}")) writer.newLine();
						writer.write(line);
						writer.newLine();
					}
				}
				writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a Blockstate JSON for the specified block using all of it's properties.
	 * @author Animefan8888
	 * @param path Starting path ie "E://Modding/forge".
	 * @param modid The modid for this blocks mod, used mainly as an identifier.
	 * @param fileName Pretty self explanatory.
	 * @param block The block that is relevant to the blockstate.
	 */
	public static void genVanillaBlockState(String path, String modid, String fileName, Block block) throws IOException {
        File json = new File(path + "/assets/" + modid + "/blockstates/" + fileName + ".json");
        File folders = new File(path + "/assets/" + modid + "/blockstates/");
        folders.mkdir();
        ImmutableMap<IProperty<?>, Comparable<?>> properties = block.getBlockState().getBaseState().getProperties();
        if (json.exists()) {
            json.delete();
        }
        JsonWriter jsonWriter = new JsonWriter(new FileWriter(json));
        jsonWriter.setIndent("    ");
        jsonWriter.setLenient(false);
        jsonWriter.beginObject();
        jsonWriter.name("variants").beginObject();
        writeVanillaProperties(properties.keySet(), jsonWriter);
        jsonWriter.endObject().endObject();
        jsonWriter.close();
	}
	
	/**
	 * Writes the properties to the JsonWriter.
	 * @param properties All of the blocks BlockState properties.
	 * @param jsonWriter The writer for the JSON.
	 */
	private static void writeVanillaProperties(Collection<IProperty<?>> properties, JsonWriter jsonWriter) throws IOException {
		String[] names = new String[properties.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = ((IProperty<?>)properties.toArray()[i]).getName();
		}
		Iterable<List<Comparable<?>>> cartesianProduct = Cartesian.cartesianProduct(getAllowedValues(properties));
		for (List<Comparable<?>> values : cartesianProduct) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < values.size(); i++) {
				builder.append(names[i] + "=" + values.get(i).toString() + ",");
			}
			builder.replace(builder.length() - 1, builder.length(), "");
			jsonWriter.name(builder.toString()).beginObject().endObject();
		}
	}
	
	/**
	 * Used to get the Caratesian Product.
	 * @param properties All of the blocks BlockState properties.
	 * @return The Collection converted to a List of a Iterable to be used in Cartesian.cartesianProduct.
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private static List < Iterable < Comparable<? >>> getAllowedValues(Collection<IProperty<?>> properties) {
        List < Iterable < Comparable<? >>> list = Lists. < Iterable < Comparable<? >>> newArrayList();
        for (IProperty<?> iproperty : properties) {
            list.add(((IProperty)iproperty).getAllowedValues());
        }
        return list;
    }
	
	/**
	 * Generates a Blockstate JSON for the specified block using all of it's properties.
	 * @author Loordgek
	 * @param path Starting path ie "E://Modding/forge".
	 * @param modid The modid for this blocks mod, used mainly as an identifier.
	 * @param fileName Pretty self explanatory.
	 * @param block The block that is relevant to the blockstate.
	 */
	public static void genForgeBlockState(String path, String modid, String fileName, Block block) throws IOException {
        File json = new File(path + "/assets/" + modid + "/blockstates/" + fileName + ".json");
        File folders = new File(path + "/assets/" + modid + "/blockstates/");
        folders.mkdir();
        ImmutableMap<IProperty<?>, Comparable<?>> properties = block.getBlockState().getBaseState().getProperties();
        if (json.exists()) {
            json.delete();
        }
        JsonWriter jsonWriter = new JsonWriter(new FileWriter(json));
        jsonWriter.setIndent("    ");
        jsonWriter.setLenient(false);
        jsonWriter.beginObject();
        jsonWriter.name("forge_marker").value(1);
        jsonWriter.name("defaults").beginObject();
        jsonWriter.endObject();
        jsonWriter.name("variants").beginObject();
        for (IProperty<?> property : properties.keySet()) {
            writeForgeProperty(property, jsonWriter);
        }
        jsonWriter.endObject();
        jsonWriter.endObject();
        jsonWriter.close();
    }
	
	/**
	 * Writes a single forge syntax property.
	 * @param property A single property for the blocks BlockState.
	 * @param jsonWriter The writer for the JSON.
	 */
    private static void writeForgeProperty(IProperty<?> property, JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(property.getName());
        jsonWriter.beginObject();
        for (Object object : property.getAllowedValues().toArray()) {
            jsonWriter.name(object.toString());
            jsonWriter.beginObject();
            jsonWriter.endObject();
        }
        jsonWriter.endObject();
}
	
}
