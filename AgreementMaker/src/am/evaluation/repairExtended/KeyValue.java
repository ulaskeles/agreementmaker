package am.evaluation.repairExtended;

public class KeyValue<K,V> {

	private K Key;
	private V Value;
	
	public KeyValue(K key, V value){
		Key = key;
		Value = value;
	}
	
	public void setKey (K key)
    {
    	Key = key;           
    }
    public K getKey()
    {
        return Key;
    }

    public void setValue (V value)
    {
    	Value = value;           
    }
    public V getValue()
    {
        return Value;
    }	
}
