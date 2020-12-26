(ns jsonbutedn.core
  (:gen-class
    :implements [net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch]
    :main false)
  (:import (net.devtech.grossfabrichacks.instrumentation InstrumentationApi)
           (com.google.gson.stream JsonReader)
           (net.devtech.grossfabrichacks.transformer.asm AsmClassTransformer)
           (org.objectweb.asm.tree ClassNode MethodNode MethodInsnNode FieldInsnNode InsnList AbstractInsnNode VarInsnNode)
           (org.objectweb.asm Opcodes Type)
           (jsonbutedn JsonButActuallyEdnReader)
           (com.google.gson JsonObject JsonArray JsonElement JsonPrimitive)))

(def tansformers
  {"<init>(Ljava/io/Reader;)V"
   (fn [^MethodNode mn]
     (if-some [^AbstractInsnNode put-in
               (some #(when (and (instance? FieldInsnNode %)
                                 (= "in" (.-name ^FieldInsnNode %)))
                        %)
                     (.-instructions mn))]
       (.insertBefore (.-instructions mn)
                      put-in
                      (MethodInsnNode. Opcodes/INVOKESTATIC
                                       (Type/getInternalName JsonButActuallyEdnReader)
                                       "wrap"
                                       "(Ljava/io/Reader;)Ljava/io/Reader;"))
       (throw (IllegalStateException. "Couldn't find field access instruction."))))})

(defn transform-json-reader [^ClassNode node]
  (doseq [^MethodNode method (.-methods node)]
    (when-some [transformer (tansformers (str (.-name method) (.-desc method)))]
      (transformer method))))

(defn -onPrePrePreLaunch [_]
  ;; JsonReader has already been read to load configurations and stuff grr
  (InstrumentationApi/retransform
    ^Class JsonReader
    (reify AsmClassTransformer
      (transform [_ _s classNode]
        (try
          (transform-json-reader classNode)
          (catch Exception e
            (.printStackTrace e)
            (throw e)))))))

#_[-onInitialize]

(defn or-func [f & gs]
  (reduce (fn [f g]
            (fn [x]
              (or (f x)
                  (g x))))
          f
          gs))

(defn edn->json
  ^JsonElement
  [obj]
  (condp #(%1 %2) obj
    map?
    (reduce
      (fn [^JsonObject jsobj [k v]]
        (.add jsobj
              (if (keyword? k)
                (subs (str k) 1)
                k)
              (edn->json v))
        jsobj)
      (JsonObject.)
      obj)

    (or-func list? vector?)
    (reduce
      (fn [^JsonArray jsarr o]
        (.add ^JsonArray jsarr (edn->json o))
        jsarr)
      (JsonArray.)
      obj)

    boolean? (JsonPrimitive. ^Boolean obj)
    number? (JsonPrimitive. ^Number obj)
    char? (JsonPrimitive. ^Character obj)
    keyword? (JsonPrimitive. (subs (str obj) 1))

    (JsonPrimitive. (str obj))))
