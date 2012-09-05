package me.hammale.koth;

import java.io.Serializable;
import java.util.HashSet;

import org.bukkit.inventory.ItemStack;

public class Kit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 432733301865026178L;
	
	private HashSet<CardboardBox> items = new HashSet<CardboardBox>();
	private HashSet<CardboardBox> armor = new HashSet<CardboardBox>();
	private boolean hasPerm;
	private String perm;

	public Kit(ItemStack[] itemz, Armor armor, String perm){
	    for(ItemStack is : itemz){
	    	if(is != null){
	    		items.add(new CardboardBox(is));
	    	}else{
	    		
	    	}
		}
	    for(ItemStack is : armor.){
	    	if(is != null){
	    		items.add(new CardboardBox(is));
	    	}
		}
		if(perm.equalsIgnoreCase("NA")){
			hasPerm = false;
		}else{
			hasPerm = true;
		}
		this.perm = perm;
	}
	
	public ItemStack[] getItems(){
		ItemStack[] tmp = new ItemStack[items.size()];
		int i = 0;
		for(CardboardBox box : items){
			tmp[i] = box.unbox();
			i++;
		}
		return tmp;
	}
	
	public ItemStack[] getArmor(){
		ItemStack[] tmp = new ItemStack[items.size()];
		int i = 0;
		for(CardboardBox box : armor){
			tmp[i] = box.unbox();
			i++;
		}
		return tmp;
	}
	
	public boolean hasPerm(){
		return hasPerm;
	}
	
	public String getPerm(){
		return this.perm;
	}
	
}
