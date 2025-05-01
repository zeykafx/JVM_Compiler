package compiler.CodeGen;

import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SlotTable {
	// same principle as the symbol table but with a currentSlot counter

	private final SlotTable parent;
	public final HashMap<String, Integer> slots;
	public final AtomicReference<Integer> currentSlot;
	private String localFunctionName;

	public SlotTable(AtomicReference<Integer> currentSlot, SlotTable parent) {
		this.parent = parent;
		this.slots = new HashMap<>();
		this.currentSlot = currentSlot;
	}

	public SlotTable getParent() {
		return parent;
	}

	public Integer addSlot(String name, org.objectweb.asm.Type type ) {
		slots.put(name, currentSlot.get());
		currentSlot.set(currentSlot.get() + type.getSize());
		return currentSlot.get() - type.getSize();
	}

	public Integer lookup(String name) {
		Integer slot = slots.get(name);
		if (slot == null && parent != null) {
			return parent.lookup(name);
		}
		if (slot == null) {
			return -1;
		}
		return slot;
	}
}

