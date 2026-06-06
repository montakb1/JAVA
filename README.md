# Plan de D\u00e9veloppement Complet \u2014 Avocat_AI / HaqiqiLaw

**Date :** 6 juin 2026
**Version :** 1.0
**Auteur :** Audit de revue compl\u00e8te du projet
**P\u00e9rim\u00e8tre :** UNIQUEMENT changements de d\u00e9veloppement (code, architecture, qualit\u00e9). Aucun aspect business.

---

## Comment lire ce plan

Chaque t\u00e2che a :
- **ID** : T-XXX (T-001, T-002, etc.) pour traquer
- **Type** : `MODIFIER` (refactor / fix) / `AJOUTER` (nouvelle feature) / `SUPPRIMER` (cleanup)
- **Priorit\u00e9** : P0 (bloquant), P1 (critique), P2 (important), P3 (futur)
- **Effort estim\u00e9** : XS (< 2h), S (2-8h), M (1-3j), L (1-2 sem), XL (> 2 sem)
- **Fichiers concern\u00e9s**
- **Pourquoi** : justification technique
- **Comment** : \u00e9tapes concr\u00e8tes
- **Crit\u00e8res d'acceptation** : comment savoir que c'est fait

---

# PHASE 1 \u2014 P0 (Bloquants avant toute mise en production)

## T-001 \u2014 [MODIFIER] Finaliser la re-indexation du corpus apr\u00e8s audit

**Priorit\u00e9 :** P0 \u2014 **Effort :** XS (1h)
**Fichiers :** Aucun (ex\u00e9cution scripts)

**Pourquoi :**
Les corrections juridiques de l'audit AUDIT_RAG_CORPUS_2026.md sont en base de fichiers mais pas encore en base de donn\u00e9es. Le diagnostic continue d'afficher les anciennes valeurs (Loi 2000-40, 7 jours) tant que la DB n'est pas re-seed\u00e9e.

**Comment :**
1. Backup de la DB actuelle : `pg_dump avocat_ai > backup_pre_audit.sql`
2. Lancer le script de re-indexation cr\u00e9\u00e9 :
   ```bash
   cd backend
   python -m scripts.reindex_legal_corpus
   ```
3. V\u00e9rifier les logs : `\u2705 LegalArticle r\u00e9-import\u00e9s` et `\u2705 RAG r\u00e9-indexation termin\u00e9e`
4. Tester le sc\u00e9nario diagnostic e-commerce \"chaussures\" et v\u00e9rifier l'affichage correct.

**Crit\u00e8res d'acceptation :**
- Le diagnostic affiche \"Loi n\u00b0 98-40\" et non plus \"Loi n\u00b0 2000-40\"
- Le d\u00e9lai de r\u00e9tractation affich\u00e9 est de **10 jours**
- Les \u00e9tapes mentionnent \"Tribunal du Travail\" et non plus \"Conseil de Prud'hommes\"

---

## T-002 \u2014 [AJOUTER] Suite de tests d'int\u00e9grit\u00e9 du corpus juridique

**Priorit\u00e9 :** P0 \u2014 **Effort :** M (2j)
**Fichiers :** `backend/tests/test_legal_corpus_integrity.py` (nouveau)

**Pourquoi :**
Sans tests, les erreurs juridiques peuvent r\u00e9appara\u00eetre \u00e0 chaque mise \u00e0 jour. Il faut une suite automatis\u00e9e qui v\u00e9rifie qu'aucun article fant\u00f4me (Art. 14 ter, Loi 2000-40, etc.) ne se glisse dans le corpus.

**Comment :**
1. Cr\u00e9er `backend/tests/test_legal_corpus_integrity.py` avec :
   - `test_no_phantom_laws()` : v\u00e9rifie qu'aucune r\u00e9f\u00e9rence \"Loi 2000-40\", \"Art. 14 ter CT\", \"Art. 23 CT pour contestation\", \"Conseil de Prud'hommes\" n'appara\u00eet dans le corpus
   - `test_known_law_articles()` : whitelist d'articles valides (Art. 14-2, 14-3, 14-4 CT, Art. 25 Loi 92-117, etc.) que le corpus doit contenir
   - `test_retractation_delay_10_days()` : assertion que tous les chunks mentionnant \"r\u00e9tractation\" mentionnent 10 jours (pas 7)
   - `test_indemnite_licenciement_correct_formula()` : v\u00e9rifie la formule \"1 jour/mois\" et non \"1 mois/an\"
2. Ajouter une whitelist `KNOWN_VALID_LAWS` et `BLACKLISTED_REFS` en t\u00eate du fichier
3. Lancer en CI \u00e0 chaque PR

**Crit\u00e8res d'acceptation :**
- `pytest backend/tests/test_legal_corpus_integrity.py` passe
- Modification d'un .txt avec une r\u00e9f\u00e9rence blacklist\u00e9e fait \u00e9chouer le test
- Test int\u00e9gr\u00e9 dans le pipeline CI

---

## T-003 \u2014 [AJOUTER] Whitelist d'articles valides dans le prompt LLM

**Priorit\u00e9 :** P0 \u2014 **Effort :** S (4h)
**Fichiers :** `backend/app/core/prompts.py`, `backend/app/services/legal_diagnostic.py`

**Pourquoi :**
Le LLM peut halluciner des articles m\u00eame avec un bon RAG. Il faut un m\u00e9canisme de validation post-g\u00e9n\u00e9ration qui v\u00e9rifie chaque r\u00e9f\u00e9rence cit\u00e9e par le LLM contre une whitelist d'articles connus.

**Comment :**
1. Cr\u00e9er `backend/app/services/legal_refs_validator.py` :
   - Charger une liste `KNOWN_REFS` depuis `tunisian_legal_refs.py`
   - Fonction `validate_llm_output(text) \u2192 (cleaned_text, warnings)` qui regex chaque \"Art. X COC/CT/CSP\" et la v\u00e9rifie
   - Si une r\u00e9f\u00e9rence n'est pas connue : la remplacer par \"[r\u00e9f\u00e9rence \u00e0 v\u00e9rifier]\" et logger
2. Appeler `validate_llm_output()` dans `_generate_summary_llm()` apr\u00e8s la r\u00e9ponse du LLM
3. Mettre \u00e0 jour le prompt syst\u00e8me pour interdire de citer des articles non fournis dans le contexte

**Crit\u00e8res d'acceptation :**
- Un test simul\u00e9 o\u00f9 le LLM cite \"Art. 999 COC\" \u2192 la sortie remplace par \"[r\u00e9f\u00e9rence \u00e0 v\u00e9rifier]\"
- Logs SQL avec compteur d'hallucinations par jour
- Aucune r\u00e9gression sur les sc\u00e9narios existants

---

## T-004 \u2014 [MODIFIER] Ajouter question \"vendeur pro/particulier\" en diagnostic conso

**Priorit\u00e9 :** P0 \u2014 **Effort :** M (1.5j)
**Fichiers :**
- Frontend : `frontend/src/app/diagnostic/page.tsx` (\u00e9tape 3)
- Backend : `backend/app/api/diagnostic.py`, `backend/app/services/legal_diagnostic.py`

**Pourquoi :**
Le diagnostic propose actuellement l'ODC et la Loi 92-117 m\u00eame quand le vendeur est un particulier sur Facebook Marketplace. Or l'ODC ne traite pas les litiges C2C et la Loi 92-117 ne s'applique pas entre particuliers.

**Comment :**
1. Frontend : Ajouter un radio button dans l'\u00e9tape 3 du diagnostic, conditionnel \u00e0 `category === \"consommation\"` :
   ```tsx
   <Field label=\"Type de vendeur *\">
     <Radio name=\"vendor_type\" value=\"professional\" label=\"Professionnel (boutique, e-commerce, marque)\" />
     <Radio name=\"vendor_type\" value=\"particular\" label=\"Particulier (Facebook Marketplace, OLX, vente entre particuliers)\" />
   </Field>
   ```
2. Backend : Ajouter `vendor_type` au schema Pydantic du diagnostic
3. Dans `legal_diagnostic.py`, ajouter une logique :
   - Si `vendor_type == \"particular\"` : forcer le pipeline \u00e0 utiliser uniquement les articles COC (Art. 647) et ignorer la Loi 92-117 et l'ODC
   - Ajouter un nouveau cas dans `STEP_TEMPLATES[\"consommation\"][\"c2c\"]` avec les bonnes \u00e9tapes (mise en demeure + tribunal directement)

**Crit\u00e8res d'acceptation :**
- Test : sc\u00e9nario \"achat sur Facebook Marketplace\" \u2192 le r\u00e9sultat ne mentionne PAS l'ODC ni la Loi 92-117
- Le r\u00e9sultat mentionne \"vices cach\u00e9s entre particuliers (Art. 647 COC)\"
- L'UI affiche un message d'avertissement clair sur la diff\u00e9rence B2C / C2C

---

## T-005 \u2014 [AJOUTER] Tests d'int\u00e9gration pour les endpoints critiques

**Priorit\u00e9 :** P0 \u2014 **Effort :** L (1 sem)
**Fichiers :** `backend/tests/` (le dossier est vide actuellement)

**Pourquoi :**
Aucun test automatis\u00e9 actuellement. Tout changement peut casser silencieusement la prod. Endpoints critiques : auth, diagnostic, RAG, document generation, OCR.

**Comment :**
1. Setup : ajouter `pytest`, `pytest-asyncio`, `httpx` (TestClient FastAPI), `factory-boy`, `faker` aux requirements
2. Cr\u00e9er `backend/tests/conftest.py` avec fixtures : DB SQLite en m\u00e9moire, client de test, utilisateurs factices
3. Cr\u00e9er les fichiers de test :
   - `tests/test_auth.py` : register, login, refresh token, password reset
   - `tests/test_diagnostic.py` : full pipeline avec 5 sc\u00e9narios connus
   - `tests/test_rag.py` : recherche de articles, embeddings
   - `tests/test_documents.py` : g\u00e9n\u00e9ration de chaque template
   - `tests/test_ocr.py` : OCR sur 3 PDFs de test (loyer, attestation, CIN)
   - `tests/test_matching.py` : matching avocat avec diff\u00e9rents crit\u00e8res
4. Mocker les appels externes (Groq, OpenAI) avec `unittest.mock` ou VCR.py

**Crit\u00e8res d'acceptation :**
- `pytest backend/tests/ -v` passe \u00e0 100%
- Couverture > 60% sur `app/api/` et `app/services/`
- Pipeline CI ex\u00e9cute ces tests \u00e0 chaque commit

---

# PHASE 2 \u2014 P1 (Critique pour la qualit\u00e9 produit)

## T-006 \u2014 [MODIFIER] Refactor de `legal_diagnostic.py` (trop gros, 600+ lignes)

**Priorit\u00e9 :** P1 \u2014 **Effort :** M (2j)
**Fichiers :** `backend/app/services/legal_diagnostic.py`

**Pourquoi :**
Ce fichier fait du domain classification, entity extraction, complexity assessment, risk assessment, summary generation. C'est 5 responsabilit\u00e9s diff\u00e9rentes. Single Responsibility Principle viol\u00e9. Tests difficiles.

**Comment :**
1. Cr\u00e9er un sous-module `backend/app/services/diagnostic/` avec :
   - `__init__.py` : expose `run_diagnostic()`
   - `domain_classifier.py` : classify_domain() + DOMAIN_KEYWORDS
   - `entity_extractor.py` : extract_entities() + tous les patterns regex
   - `complexity_assessor.py` : assess_complexity()
   - `risk_assessor.py` : _assess_risk() + COMPLEXITY_INDICATORS
   - `summary_generator.py` : _generate_summary_llm() + _generate_summary()
   - `severity_scorer.py` : _compute_severity_score()
   - `pipeline.py` : run_diagnostic() orchestrant les modules ci-dessus
2. Garder `legal_diagnostic.py` comme fa\u00e7ade qui r\u00e9-exporte pour ne pas casser les imports existants
3. Ajouter des tests unitaires par module dans `tests/diagnostic/`

**Crit\u00e8res d'acceptation :**
- Aucun fichier > 250 lignes
- Tests unitaires par sous-module
- Aucune r\u00e9gression sur les diagnostics actuels

---

## T-007 \u2014 [MODIFIER] Async/await sur les appels LLM (actuellement synchrones)

**Priorit\u00e9 :** P1 \u2014 **Effort :** M (2j)
**Fichiers :** `backend/app/services/langchain_config.py`, `backend/app/services/groq_service.py`, tous les services qui appellent `generate_llm_response`

**Pourquoi :**
FastAPI est asynchrone mais les appels Groq/OpenAI sont synchrones (bloquants). Sous charge, un seul utilisateur lent peut bloquer tout le worker. Goulot d'\u00e9tranglement majeur.

**Comment :**
1. Remplacer `requests` par `httpx.AsyncClient` dans `groq_service.py`
2. Cr\u00e9er `async def generate_llm_response_async()` dans `langchain_config.py`
3. Convertir les endpoints API qui utilisent le LLM en `async def`
4. Mettre \u00e0 jour les services appelants pour `await` les r\u00e9ponses
5. Garder la version synchrone pour les scripts (`scripts/` qui sont des CLI batchs)

**Crit\u00e8res d'acceptation :**
- Benchmark : 50 requ\u00eates simultan\u00e9es sur `/diagnostic` \u2192 latence p95 r\u00e9duite d'au moins 40%
- Aucune r\u00e9gression fonctionnelle
- Tests d'int\u00e9gration passent

---

## T-008 \u2014 [AJOUTER] Cache Redis sur les requ\u00eates RAG fr\u00e9quentes

**Priorit\u00e9 :** P1 \u2014 **Effort :** M (1.5j)
**Fichiers :** `backend/requirements.txt`, `backend/app/core/cache.py` (nouveau), `backend/app/services/rag_engine.py`

**Pourquoi :**
Le m\u00eame embedding peut \u00eatre calcul\u00e9 1000x pour la m\u00eame question (\"comment divorcer en Tunisie ?\"). Co\u00fbt OpenAI + latence. Un cache Redis cl\u00e9 = hash(query), valeur = embedding + r\u00e9sultats, TTL 7 jours.

**Comment :**
1. Ajouter `redis` au requirements
2. Cr\u00e9er `backend/app/core/cache.py` avec un wrapper `cached(key_fn, ttl=604800)`
3. D\u00e9corer `generate_embedding()` et `search_similar_chunks()` avec `@cached`
4. Ajouter une variable d'env `REDIS_URL=redis://localhost:6379/0`
5. Ajouter Redis au `docker-compose.yml`
6. Endpoint admin `/admin/cache/clear` pour invalider apr\u00e8s re-index

**Crit\u00e8res d'acceptation :**
- Hit rate du cache > 30% apr\u00e8s 1 semaine d'utilisation
- Latence du diagnostic divis\u00e9e par 3 sur requ\u00eate r\u00e9currente
- Co\u00fbt OpenAI embeddings r\u00e9duit

---

## T-009 \u2014 [MODIFIER] Streaming des r\u00e9ponses du chat (actuellement bloquant)

**Priorit\u00e9 :** P1 \u2014 **Effort :** M (2j)
**Fichiers :** `backend/app/api/chat.py`, `frontend/src/app/dashboard/citizen/chat/page.tsx`

**Pourquoi :**
L'utilisateur attend 5-15 secondes en regardant un spinner pendant que le LLM g\u00e9n\u00e8re. ChatGPT-like streaming r\u00e9duit la perception de latence de 80%.

**Comment :**
1. Backend : convertir `/chat/message` en `StreamingResponse` avec `text/event-stream` (SSE)
2. Utiliser `groq_client.chat.completions.create(stream=True)` au lieu de bloquant
3. Yielder chaque token au fur et \u00e0 mesure
4. Frontend : utiliser `EventSource` (ou `fetch` avec ReadableStream) c\u00f4t\u00e9 React
5. Afficher progressivement les tokens dans la bulle de chat
6. Gestion d'erreur : annulation de stream si l'utilisateur ferme le chat

**Crit\u00e8res d'acceptation :**
- TTFB (Time To First Byte) < 500ms
- Affichage progressif des mots dans le chat
- Bouton \"Stop\" qui interrompt la g\u00e9n\u00e9ration

---

## T-010 \u2014 [AJOUTER] Dashboard avocat fonctionnel (actuellement vide)

**Priorit\u00e9 :** P1 \u2014 **Effort :** XL (2 sem)
**Fichiers :** `frontend/src/app/dashboard/lawyer/`, `backend/app/api/lawyer_dashboard.py` (nouveau)

**Pourquoi :**
Capture utilisateur montre que le dashboard avocat est vide (\"Aucun dossier en attente\"). Sans dashboard fonctionnel, l'avocat n'a aucune raison de s'abonner.

**Comment :**
1. Backend : cr\u00e9er les endpoints API :
   - `GET /lawyer/dashboard/stats` : revenus, dossiers actifs, taux de conversion
   - `GET /lawyer/cases` : liste paginated des dossiers (en attente, actifs, r\u00e9solus)
   - `POST /lawyer/cases/{id}/accept` : accepter un dossier
   - `POST /lawyer/cases/{id}/decline` : refuser
   - `GET /lawyer/clients/{id}/messages` : messagerie client-avocat
   - `POST /lawyer/cases/{id}/invoice` : g\u00e9n\u00e9rer une facture
2. Frontend : pages Next.js avec composants :
   - `dashboard/lawyer/page.tsx` : KPIs en cards (CA mois, dossiers actifs, notation moyenne)
   - `dashboard/lawyer/cases/page.tsx` : tableau filtrable
   - `dashboard/lawyer/cases/[id]/page.tsx` : d\u00e9tail dossier + messagerie
   - `dashboard/lawyer/calendar/page.tsx` : agenda (composant `react-big-calendar`)
   - `dashboard/lawyer/clients/page.tsx` : liste clients + historique
   - `dashboard/lawyer/invoices/page.tsx` : factures
3. Charts : Recharts pour les KPIs

**Crit\u00e8res d'acceptation :**
- Avocat peut accepter/refuser des dossiers
- KPIs s'affichent correctement avec des donn\u00e9es r\u00e9elles
- Messagerie client-avocat fonctionnelle
- Calendrier avec rendez-vous

---

## T-011 \u2014 [MODIFIER] Internationalisation propre (FR/AR) avec next-intl

**Priorit\u00e9 :** P1 \u2014 **Effort :** L (1 sem)
**Fichiers :** `frontend/src/locales/` (nouveau), `frontend/src/i18n.ts` (nouveau), toutes les pages

**Pourquoi :**
Actuellement les textes FR/AR sont hardcod\u00e9s dans les composants (ex: `Bonjour, \u0623\u062d\u0645\u062f`). Impossible d'ajouter une nouvelle langue ou de basculer dynamiquement.

**Comment :**
1. Installer `next-intl` : `npm install next-intl`
2. Cr\u00e9er `frontend/src/locales/fr.json` et `ar.json` avec toutes les cl\u00e9s
3. Wrapper l'app dans `NextIntlClientProvider`
4. Remplacer tous les strings hardcod\u00e9s par `t(\"key\")`
5. Ajouter un toggle de langue dans le header
6. G\u00e9rer le RTL pour l'arabe avec un attribut `dir=\"rtl\"` sur `<html>` quand `locale === \"ar\"`

**Crit\u00e8res d'acceptation :**
- Toggle FR \u2194 AR fonctionne sur toutes les pages
- Layout RTL correct en arabe
- Aucun string hardcod\u00e9 restant (audit avec script `grep`)

---

## T-012 \u2014 [AJOUTER] Notifications toast + error boundaries

**Priorit\u00e9 :** P1 \u2014 **Effort :** S (1j)
**Fichiers :** `frontend/src/components/ui/toast.tsx`, `frontend/src/components/ErrorBoundary.tsx`

**Pourquoi :**
Actuellement les erreurs API se manifestent par silence ou crash. Les succ\u00e8s d'action (\"PDF g\u00e9n\u00e9r\u00e9\") ne sont pas confirm\u00e9s.

**Comment :**
1. Installer `sonner` (toast l\u00e9ger pour React)
2. Cr\u00e9er un `<Toaster />` global dans `layout.tsx`
3. Wrapper toutes les pages avec un `<ErrorBoundary>` qui affiche un fallback amical
4. Remplacer tous les `alert()` et `console.error()` par `toast.success()` / `toast.error()`
5. Logger les erreurs c\u00f4t\u00e9 backend via un endpoint `/api/log-error`

**Crit\u00e8res d'acceptation :**
- Toute action utilisateur (g\u00e9n\u00e9rer PDF, envoyer message, etc.) affiche une confirmation
- Toute erreur API affiche un toast rouge explicite
- Aucun `alert()` natif restant

---

## T-013 \u2014 [SUPPRIMER] Nettoyage des scripts dupliqu\u00e9s et obsol\u00e8tes

**Priorit\u00e9 :** P1 \u2014 **Effort :** S (4h)
**Fichiers :** `backend/scripts/` (40+ fichiers actuellement)

**Pourquoi :**
Le dossier `scripts/` a accumul\u00e9 50+ fichiers avec des versions multiples (`download_extra_forms.py`, `_v2`, `_v3`) et beaucoup d'obsol\u00e8tes. Impossible de savoir lequel utiliser.

**Comment :**
1. Auditer chaque script : utilis\u00e9 ? \u00e0 jour ? r\u00e9sultat reproduit-il celui des autres versions ?
2. Liste \u00e0 SUPPRIMER (\u00e0 confirmer apr\u00e8s test) :
   - `download_extra_forms.py`, `download_extra_forms_v2.py` (garder uniquement v3)
   - `download_tn_forms.py`, `download_tn_forms_v2.py` (garder le v2 le plus r\u00e9cent)
   - `scrape_idaraty_forms.py`, `scrape_idaraty_forms_v2.py` (garder le scraper_idaraty_pw.py qui utilise Playwright)
3. D\u00e9placer dans `scripts/archive/` les anciennes versions (au lieu de supprimer directement)
4. Cr\u00e9er un `scripts/README.md` listant chaque script avec : but, quand l'utiliser, d\u00e9pendances
5. Renommer les scripts pour qu'ils suivent un pattern coh\u00e9rent : `verb_object.py` (`scrape_idaraty.py`, `import_pdfs.py`)

**Crit\u00e8res d'acceptation :**
- Dossier `scripts/` r\u00e9duit \u00e0 ~20 fichiers
- `scripts/README.md` documente chaque script
- Aucune perte de fonctionnalit\u00e9 (test : le pipeline `process_all_sources.py` fonctionne)

---

## T-014 \u2014 [SUPPRIMER] Code legacy dans `services/_deprecated/`

**Priorit\u00e9 :** P1 \u2014 **Effort :** XS (1h)
**Fichiers :** `backend/app/services/_deprecated/`

**Pourquoi :**
Ce dossier accumule des services obsol\u00e8tes. Soit ils sont r\u00e9utilis\u00e9s (alors les sortir), soit ils ne le sont plus (alors les supprimer). Pas de demi-mesure.

**Comment :**
1. Lister les fichiers dans `_deprecated/`
2. Pour chacun : `grep -r \"from app.services._deprecated\" backend/` \u2192 si aucune r\u00e9f\u00e9rence, supprimer
3. Si r\u00e9f\u00e9renc\u00e9, soit migrer le code dans un service actuel, soit garder mais documenter pourquoi

**Crit\u00e8res d'acceptation :**
- Dossier `_deprecated/` supprim\u00e9 OU r\u00e9duit \u00e0 ce qui est r\u00e9ellement encore utilis\u00e9 (avec README expliquant pourquoi)

---

## T-015 \u2014 [MODIFIER] Consolidation `document_variables.py` et `document_variables_v2.py`

**Priorit\u00e9 :** P1 \u2014 **Effort :** S (4h)
**Fichiers :** `backend/app/services/document_variables.py`, `document_variables_v2.py`

**Pourquoi :**
Avoir deux versions cr\u00e9e de la confusion : laquelle est appel\u00e9e ? Les deux ? Risque de divergence silencieuse.

**Comment :**
1. Identifier les usages de chacun : `grep -r \"from app.services.document_variables\" backend/`
2. Si `v2` est strictement sup\u00e9rieur : migrer tous les imports vers `v2`, puis renommer `v2 \u2192 document_variables.py` et supprimer l'ancien
3. Si les deux ont des features distinctes : merger dans un seul fichier avec `from .legacy import ...`

**Crit\u00e8res d'acceptation :**
- Un seul fichier `document_variables.py` reste
- Tous les tests passent
- Aucun import bris\u00e9

---

## T-016 \u2014 [MODIFIER] Supprimer le dossier `uploads/` dupliqu\u00e9 \u00e0 la racine

**Priorit\u00e9 :** P1 \u2014 **Effort :** XS (1h)
**Fichiers :** `Avocat_AI/uploads/` (racine), `Avocat_AI/backend/uploads/`

**Pourquoi :**
Il y a deux dossiers uploads (racine + backend). Confusion : lequel est utilis\u00e9 ? Risque de fichiers orphelins.

**Comment :**
1. V\u00e9rifier dans `app/core/config.py` quel chemin est utilis\u00e9 : `UPLOAD_DIR`
2. Si c'est `backend/uploads/` : d\u00e9placer le contenu de `Avocat_AI/uploads/` vers `backend/uploads/` et supprimer le premier
3. Ajouter `uploads/` au `.gitignore` (les uploads utilisateurs ne doivent pas \u00eatre commit\u00e9s)
4. Documenter dans README la structure des dossiers de donn\u00e9es

**Crit\u00e8res d'acceptation :**
- Un seul dossier uploads
- `.gitignore` mis \u00e0 jour
- Aucun fichier upload\u00e9 perdu

---

# PHASE 3 \u2014 P2 (Important pour la maturit\u00e9 produit)

## T-017 \u2014 [AJOUTER] Signature \u00e9lectronique TUNTRUST/NGSign

**Priorit\u00e9 :** P2 \u2014 **Effort :** XL (2 sem)
**Fichiers :** `backend/app/services/signature_service.py` (nouveau), `backend/app/api/signatures.py` (nouveau), `frontend/src/app/dashboard/citizen/sign/page.tsx` (nouveau)

**Pourquoi :**
Tes documents g\u00e9n\u00e9r\u00e9s n'ont aucune valeur probante sans signature \u00e9lectronique l\u00e9galement valable. C'est aussi le gap principal vs e-Tafakna.

**Comment :**
1. S'inscrire au programme partenaire TUNTRUST (ANCE) ou utiliser DocuSign en bouchon le temps de l'int\u00e9gration officielle
2. Cr\u00e9er `signature_service.py` avec :
   - `request_signature(document_id, signers: list[Signer])` \u2192 cr\u00e9e une enveloppe
   - `get_signature_status(envelope_id)`
   - `download_signed_document(envelope_id)`
3. Mod\u00e8le SQLAlchemy `SignatureEnvelope` avec statuts (pending, signed, declined, expired)
4. Webhooks pour recevoir les notifications de signature
5. Frontend : flow \"Envoyer pour signature\" sur un document g\u00e9n\u00e9r\u00e9 \u2192 saisie des signataires \u2192 envoi
6. Page \"Mes documents \u00e0 signer\" pour les destinataires

**Crit\u00e8res d'acceptation :**
- Un document peut \u00eatre envoy\u00e9 pour signature \u00e0 plusieurs parties
- Statut affich\u00e9 en temps r\u00e9el
- PDF sign\u00e9 t\u00e9l\u00e9chargeable avec certificat de signature

---

## T-018 \u2014 [AJOUTER] Workflow de collaboration multi-parties sur documents

**Priorit\u00e9 :** P2 \u2014 **Effort :** L (1.5 sem)
**Fichiers :** Backend models, API, frontend dashboard

**Pourquoi :**
Aujourd'hui un document est g\u00e9n\u00e9r\u00e9 puis t\u00e9l\u00e9charg\u00e9 en local. Aucune collaboration possible. e-Tafakna fait \u00e7a tr\u00e8s bien (commentaires, validation, suivi).

**Comment :**
1. Mod\u00e8le `DocumentCollaboration` avec statuts : `draft \u2192 reviewing \u2192 approved \u2192 signed \u2192 archived`
2. Mod\u00e8le `DocumentComment` avec position (page + coord) et r\u00e9solution
3. API : `POST /documents/{id}/invite`, `POST /documents/{id}/comments`, `PATCH /documents/{id}/status`
4. Frontend : visualiseur PDF avec annotations (utiliser `react-pdf` + `react-pdf-annotator`)
5. Notifications email aux parties prenantes \u00e0 chaque changement

**Crit\u00e8res d'acceptation :**
- Un avocat peut envoyer un brouillon \u00e0 son client
- Le client peut commenter une clause sp\u00e9cifique
- L'avocat re\u00e7oit une notification et peut r\u00e9pondre
- Workflow approval traceable

---

## T-019 \u2014 [AJOUTER] Outils gratuits SEO (calculateurs juridiques)

**Priorit\u00e9 :** P2 \u2014 **Effort :** M (3j)
**Fichiers :** `frontend/src/app/outils/` (nouveau)

**Pourquoi :**
e-Tafakna a 7 outils gratuits (cong\u00e9s, indemnit\u00e9, NDA...) qui leur ram\u00e8nent du trafic SEO. Toi tu n'as rien d'\u00e9quivalent.

**Comment :**
Cr\u00e9er les pages publiques (pas d'authentification requise) :
1. `/outils/calculateur-indemnite-licenciement` : formule \"1 jour/mois\" Art. 22 CT (apr\u00e8s correction audit), inputs : salaire mensuel + anciennet\u00e9 en mois
2. `/outils/calculateur-conges-payes` : 2 jours/mois de travail = 24 jours/an + jours suppl\u00e9mentaires anciennet\u00e9
3. `/outils/calculateur-preavis` : Art. 14-2 CT (1 mois si < 5 ans, 2 mois si \u2265 5 ans)
4. `/outils/comparateur-sarl-suarl` : tableau comparatif interactif
5. `/outils/generateur-mise-en-demeure` : mini version libre (sans signature) \u2192 incite \u00e0 cr\u00e9er un compte
6. `/outils/calculateur-pension-alimentaire` : estimation indicative
7. `/outils/checklist-creation-entreprise` : checklist t\u00e9l\u00e9chargeable

Chaque outil a son meta-tags SEO, structured data JSON-LD, et bouton CTA \"Cr\u00e9er un compte pour aller plus loin\".

**Crit\u00e8res d'acceptation :**
- 7 outils en ligne
- Lighthouse SEO score > 90 sur chaque
- Tracking analytics pour mesurer les conversions

---

## T-020 \u2014 [AJOUTER] Blog SEO avec articles juridiques

**Priorit\u00e9 :** P2 \u2014 **Effort :** L (1 sem dev + contenu \u00e0 part)
**Fichiers :** `frontend/src/app/blog/`, `backend/app/api/blog.py`

**Pourquoi :**
e-Tafakna publie des articles SEO sur \"Contrat de travail tunisien 2026\", \"Facture \u00e9lectronique TEIF\", etc. Toi tu n'as rien. Manque de trafic organique.

**Comment :**
1. Backend : mod\u00e8le `BlogPost` avec title, slug, content (markdown), excerpt, cover_image, published_at, category, tags, author
2. API CRUD pour admin
3. Frontend public :
   - `/blog` : liste paginated, filtres par cat\u00e9gorie
   - `/blog/[slug]` : article complet avec markdown render (`react-markdown`)
4. Generation static (`generateStaticParams`) pour le SEO
5. Sitemap.xml g\u00e9n\u00e9r\u00e9 automatiquement
6. Structured data Article JSON-LD
7. Admin UI pour publier des articles

**Crit\u00e8res d'acceptation :**
- Editor markdown dans le dashboard admin
- Articles indexables par Google (sitemap + meta tags)
- Search bar dans la liste

---

## T-021 \u2014 [AJOUTER] Module cr\u00e9ation SARL/SUARL

**Priorit\u00e9 :** P2 \u2014 **Effort :** XL (2 sem)
**Fichiers :** `backend/app/api/company_creation.py`, `frontend/src/app/dashboard/citizen/creer-societe/`

**Pourquoi :**
e-Tafakna en fait son flagship. Tr\u00e8s gros march\u00e9 en Tunisie (~30 000 cr\u00e9ations/an).

**Comment :**
Wizard multi-\u00e9tapes :
1. Choix du type : SARL vs SUARL (avec comparateur)
2. Identit\u00e9 du/des associ\u00e9s : CIN, adresse, parts
3. Information soci\u00e9t\u00e9 : nom, capital, si\u00e8ge, activit\u00e9 (code APE)
4. G\u00e9n\u00e9ration automatique :
   - Statuts (template HTML \u2192 PDF)
   - PV de constitution
   - D\u00e9claration d'existence
   - Demande de matricule fiscal
   - Demande d'immatriculation RNE
5. Checklist des d\u00e9marches \u00e0 effectuer (avec liens vers idaraty)
6. Tracking de l'avancement

**Crit\u00e8res d'acceptation :**
- Wizard complet en 5-6 \u00e9tapes
- 6 documents g\u00e9n\u00e9r\u00e9s correctement
- Tracking d'avancement persistant

---

## T-022 \u2014 [AJOUTER] Calendrier \u00e9chances l\u00e9gales pour citoyens

**Priorit\u00e9 :** P2 \u2014 **Effort :** M (3j)
**Fichiers :** `backend/app/services/legal_calendar.py` (existe d\u00e9j\u00e0), `frontend/src/app/dashboard/citizen/calendar/`

**Pourquoi :**
Le service `legal_calendar.py` existe mais aucune UI ne l'expose. Pourtant tr\u00e8s utile : un licenciement \u2192 d\u00e9lai 1 an, garde l'\u00e9ch\u00e9ance dans le calendrier.

**Comment :**
1. Backend : enrichir `legal_calendar.py` pour cr\u00e9er des \u00e9v\u00e9nements depuis un diagnostic (\"vous avez un licenciement, ajoutez l'\u00e9ch\u00e9ance des 12 mois\")
2. Frontend : composant calendrier (`react-big-calendar`)
3. Notifications push 7 jours avant et 1 jour avant
4. Export iCal pour Google Calendar / Apple Calendar

**Crit\u00e8res d'acceptation :**
- Apr\u00e8s un diagnostic, l'utilisateur peut \"Ajouter l'\u00e9ch\u00e9ance \u00e0 mon calendrier\"
- Vue calendrier mensuelle/hebdomadaire
- Export iCal fonctionnel

---

## T-023 \u2014 [AJOUTER] Module facturation \u00e9lectronique (TEIF/TTN)

**Priorit\u00e9 :** P2 \u2014 **Effort :** XL (3 sem)
**Fichiers :** `backend/app/services/einvoice_service.py` (nouveau), `backend/app/api/einvoice.py`

**Pourquoi :**
La facturation \u00e9lectronique devient obligatoire en Tunisie en 2026 pour beaucoup d'entreprises. Cible directe pour les avocats/cabinets.

**Comment :**
1. Inscription au programme TTN (Tunisie TradeNet)
2. G\u00e9n\u00e9rateur de facture conforme au format TEIF (XML)
3. Connexion API TTN pour soumettre les factures
4. Suivi des factures (\u00e9mises, valid\u00e9es, rejet\u00e9es)
5. Export comptable mensuel
6. Module Avocat : factures de prestations clients

**Crit\u00e8res d'acceptation :**
- Une facture est g\u00e9n\u00e9r\u00e9e au format TEIF XML valide
- Soumission TTN r\u00e9ussie en environnement de test
- Tracking du statut

---

## T-024 \u2014 [MODIFIER] App mobile : compl\u00e9ter les screens manquants

**Priorit\u00e9 :** P2 \u2014 **Effort :** L (1.5 sem)
**Fichiers :** `mobile/app/`

**Pourquoi :**
L'app Expo a des dossiers mais probablement plusieurs screens incomplets. Avant le build store, il faut compl\u00e9ter.

**Comment :**
Audit complet de chaque screen :
1. `(auth)/login.tsx`, `register.tsx` : v\u00e9rifier les validations
2. `(tabs)/home.tsx` : dashboard utilisateur
3. `(tabs)/chat.tsx` : streaming chat
4. `(tabs)/documents.tsx` : liste + g\u00e9n\u00e9ration PDF
5. `(tabs)/procedures.tsx` : recherche procedures
6. `(tabs)/scanner.tsx` : utiliser `expo-camera` + cropping
7. `(tabs)/lawyers.tsx` : matching
8. `(tabs)/profile.tsx` : \u00e9dition profil
9. Push notifications avec `expo-notifications`
10. Offline mode : cache des procedures dans AsyncStorage

**Crit\u00e8res d'acceptation :**
- Chaque tab fonctionne end-to-end
- Push notifications re\u00e7ues sur Android et iOS
- Mode offline pour les 100 procedures les plus consult\u00e9es

---

## T-025 \u2014 [AJOUTER] Background tasks avec Celery + Redis

**Priorit\u00e9 :** P2 \u2014 **Effort :** M (2j)
**Fichiers :** `backend/app/core/celery_app.py` (nouveau), `backend/app/tasks/`

**Pourquoi :**
Aujourd'hui l'OCR, l'extraction de PDF, les emails sont fait dans le m\u00eame request thread \u2192 timeout possible. Celery les ex\u00e9cute en arri\u00e8re-plan.

**Comment :**
1. Ajouter `celery[redis]` et `flower` (monitoring) aux requirements
2. Cr\u00e9er `celery_app.py` configur\u00e9 avec Redis
3. Migrer les fonctions co\u00fbteuses en tasks :
   - `ocr_pdf_task(file_path)`
   - `send_email_task(to, subject, body)`
   - `generate_pdf_task(template, data)`
   - `compute_embeddings_batch_task()`
4. Ajouter Celery worker au `docker-compose.yml`
5. Frontend : polling de l'\u00e9tat de la t\u00e2che ou WebSocket

**Crit\u00e8res d'acceptation :**
- L'utilisateur uploade un PDF de 50 pages \u2192 r\u00e9ponse imm\u00e9diate \"En cours...\" + notification quand pr\u00eat
- Flower accessible sur localhost:5555 pour monitoring
- Aucun endpoint API > 5s de latence

---

## T-026 \u2014 [AJOUTER] Pre-commit hooks (linting, formatting, tests rapides)

**Priorit\u00e9 :** P2 \u2014 **Effort :** S (4h)
**Fichiers :** `.pre-commit-config.yaml` (nouveau), `pyproject.toml`

**Pourquoi :**
\u00c9viter de commit du code mal format\u00e9 ou non lint\u00e9. R\u00e9duit les diffs et la dette technique.

**Comment :**
1. Installer `pre-commit` et configurer `.pre-commit-config.yaml` avec :
   - `black` (formatter Python)
   - `isort` (import order)
   - `flake8` + `flake8-bugbear`
   - `mypy` (type checking)
   - `prettier` (frontend JS/TS/JSON)
   - `eslint --fix`
2. Ajouter `pyproject.toml` pour la config Black + isort
3. `npm run lint` et `npm run format` en pre-commit
4. Document dans README

**Crit\u00e8res d'acceptation :**
- `git commit` \u00e9choue si code mal format\u00e9
- Pipeline CI lance les m\u00eames checks
- Tous les fichiers existants pass\u00e9s au formatter une fois (gros commit \"chore: format\")

---

## T-027 \u2014 [AJOUTER] CI/CD GitHub Actions

**Priorit\u00e9 :** P2 \u2014 **Effort :** M (2j)
**Fichiers :** `.github/workflows/`

**Pourquoi :**
Sans CI, aucune protection contre les r\u00e9gressions. Sans CD, deployment manuel = risque erreurs.

**Comment :**
1. `.github/workflows/backend-tests.yml` : lance pytest \u00e0 chaque PR sur backend/
2. `.github/workflows/frontend-tests.yml` : lance `npm test` + `npm run lint`
3. `.github/workflows/security-scan.yml` : `bandit` (Python security), `npm audit`
4. `.github/workflows/legal-corpus-check.yml` : lance le test d'int\u00e9grit\u00e9 T-002 \u00e0 chaque modif de `data/`
5. `.github/workflows/deploy-staging.yml` : auto-deploy sur staging \u00e0 chaque merge dans `develop`
6. Badges README

**Crit\u00e8res d'acceptation :**
- 4 workflows en place
- PR ne peut \u00eatre merg\u00e9e que si CI green
- Notifications Slack en cas d'\u00e9chec

---

## T-028 \u2014 [AJOUTER] Sentry pour error tracking en prod

**Priorit\u00e9 :** P2 \u2014 **Effort :** XS (2h)
**Fichiers :** Backend + Frontend

**Pourquoi :**
Aujourd'hui une exception en prod = invisible. Pas de m\u00e9trique sur le taux d'erreur.

**Comment :**
1. Cr\u00e9er un projet Sentry (free tier suffit au d\u00e9but)
2. Backend : `pip install sentry-sdk[fastapi]`, init dans `main.py`
3. Frontend : `npm install @sentry/nextjs`, configurer
4. Tags : `environment`, `release` (avec le commit SHA)
5. Alertes : email/Slack quand error rate > X/heure

**Crit\u00e8res d'acceptation :**
- Une exception lev\u00e9e en backend appara\u00eet dans Sentry sous 30s
- Source maps frontend uploaded
- Alertes configur\u00e9es

---

## T-029 \u2014 [MODIFIER] Optimisation Docker pour production

**Priorit\u00e9 :** P2 \u2014 **Effort :** S (4h)
**Fichiers :** `backend/Dockerfile`, `frontend/Dockerfile`, `docker-compose.prod.yml`

**Pourquoi :**
Les Dockerfiles actuels font probablement 1.5GB+ avec d\u00e9pendances dev incluses. Trop lent au cold start, co\u00fbteux en stockage registry.

**Comment :**
1. Multi-stage build :
   - Stage 1 (builder) : install d\u00e9pendances + compile
   - Stage 2 (runtime) : copy uniquement les artefacts compil\u00e9s + d\u00e9pendances runtime
2. Base image `python:3.11-slim` au lieu de `python:3.11`
3. `.dockerignore` pour exclure `venv/`, `__pycache__/`, `tests/`, `docs/`, `*.log`
4. Frontend : utiliser `next.js standalone` pour image plus l\u00e9g\u00e8re
5. Healthchecks dans le Dockerfile

**Crit\u00e8res d'acceptation :**
- Image backend < 500MB
- Image frontend < 200MB
- Cold start < 5s

---

## T-030 \u2014 [AJOUTER] Rate limiting par endpoint

**Priorit\u00e9 :** P2 \u2014 **Effort :** S (4h)
**Fichiers :** `backend/app/core/rate_limit.py` (nouveau)

**Pourquoi :**
Aujourd'hui un utilisateur malveillant peut spammer `/diagnostic` (100\u20ac d'embeddings) ou `/chat` (LLM \u00e0 chaque message). Protection requise.

**Comment :**
1. `pip install slowapi` (rate limit pour FastAPI)
2. Configurer Redis comme backend
3. D\u00e9corer chaque endpoint co\u00fbteux :
   ```python
   @limiter.limit(\"10/minute\")
   @router.post(\"/diagnostic\")
   ```
4. Quotas diff\u00e9renci\u00e9s par plan d'abonnement (FREE: 5/jour, PRO: 100/jour, etc.)
5. R\u00e9ponse HTTP 429 avec header `Retry-After`

**Crit\u00e8res d'acceptation :**
- Test : 11 requ\u00eates /min sur `/diagnostic` \u2192 la 11\u00e8me retourne 429
- Diff\u00e9renciation par tier d'abonnement

---

# PHASE 4 \u2014 P3 (Futur / Long terme)

## T-031 \u2014 [AJOUTER] M\u00e9diation en ligne (HiiL Justice Accelerator)

**Priorit\u00e9 :** P3 \u2014 **Effort :** XL (3-4 sem)

Module de m\u00e9diation entre parties avec un m\u00e9diateur agr\u00e9\u00e9 (visio + chat + annotations sur documents). Postuler au HiiL Justice Accelerator pour le financement.

---

## T-032 \u2014 [AJOUTER] Voice transcription real-time (Whisper streaming)

**Priorit\u00e9 :** P3 \u2014 **Effort :** L (1.5 sem)

L'avatar 3D actuel attend la fin de la parole. Avec Whisper streaming, l'utilisateur voit ses mots transcrits en temps r\u00e9el.

---

## T-033 \u2014 [AJOUTER] Embeddings tunisien-sp\u00e9cifiques (fine-tuning)

**Priorit\u00e9 :** P3 \u2014 **Effort :** XL (1 mois)

Les embeddings OpenAI sont g\u00e9n\u00e9riques. Fine-tuner un modèle sur le corpus juridique tunisien (dialecte + arabe classique + fran\u00e7ais juridique) am\u00e9liorerait la pertinence RAG.

---

## T-034 \u2014 [AJOUTER] Mode hors-ligne progressif (PWA)

**Priorit\u00e9 :** P3 \u2014 **Effort :** L (1 sem)

Convertir l'app Next.js en PWA (manifest + service worker) pour permettre la consultation des proc\u00e9dures et de l'historique en mode offline.

---

## T-035 \u2014 [AJOUTER] Storybook pour les composants frontend

**Priorit\u00e9 :** P3 \u2014 **Effort :** M (3j)

Documenter visuellement chaque composant UI (Button, Card, Form, etc.) avec Storybook. Facilite l'onboarding nouveaux d\u00e9veloppeurs.

---

## T-036 \u2014 [AJOUTER] A/B testing framework

**Priorit\u00e9 :** P3 \u2014 **Effort :** M (3j)

Tester diff\u00e9rentes versions de prompts LLM, de copy frontend, de pricing. Outil : Statsig ou GrowthBook.

---

## T-037 \u2014 [AJOUTER] Analytics produit (PostHog ou Mixpanel)

**Priorit\u00e9 :** P3 \u2014 **Effort :** S (1j)

Tracking des \u00e9v\u00e9nements clients : diagnostic lanc\u00e9, document g\u00e9n\u00e9r\u00e9, avocat contact\u00e9. Permet d'identifier les goulots d'\u00e9tranglement du funnel.

---

## T-038 \u2014 [AJOUTER] Two-Factor Authentication (2FA)

**Priorit\u00e9 :** P3 \u2014 **Effort :** M (2j)

Pour les avocats (compte sensible), proposer 2FA via TOTP (Google Authenticator) ou SMS.

---

# Synth\u00e8se par effort

| Phase | T\u00e2ches | Effort total |
|---|---|---|
| **Phase 1 (P0)** | 5 t\u00e2ches | ~2 semaines |
| **Phase 2 (P1)** | 11 t\u00e2ches | ~5-6 semaines |
| **Phase 3 (P2)** | 14 t\u00e2ches | ~10-12 semaines |
| **Phase 4 (P3)** | 8 t\u00e2ches | ~12-16 semaines |
| **TOTAL** | **38 t\u00e2ches** | **~30-36 semaines (7-9 mois)** |

---

# Ordonnancement recommand\u00e9 (sprint planning sur 6 mois)

## Sprint 1-2 (semaines 1-4) : Stabilisation P0
T-001, T-002, T-003, T-004, T-005

## Sprint 3-4 (semaines 5-8) : Qualit\u00e9 interne P1
T-006, T-007, T-013, T-014, T-015, T-016

## Sprint 5-6 (semaines 9-12) : Frontend critique P1
T-008, T-009, T-011, T-012

## Sprint 7-9 (semaines 13-18) : Dashboard avocat P1
T-010

## Sprint 10-13 (semaines 19-26) : Features diff\u00e9renciantes P2
T-017 (signature), T-018 (collaboration), T-019 (outils SEO), T-020 (blog)

## Sprint 14-16 (semaines 27-32) : Modules m\u00e9tier P2
T-021 (cr\u00e9ation SARL), T-022 (calendrier), T-024 (mobile)

## Sprint 17+ (semaines 33+) : Infra & long terme
T-025 (Celery), T-026 (pre-commit), T-027 (CI/CD), T-028 (Sentry), T-029 (Docker), T-030 (rate limit)

---

# Recommandations transverses

1. **Faire des PRs petites** : 1 t\u00e2che = 1 PR. \u00c9viter les PRs de 50+ fichiers.
2. **Documenter au fur et \u00e0 mesure** : chaque nouveau service \u2192 docstring + entr\u00e9e dans ARCHITECTURE.md.
3. **Mesurer avant d'optimiser** : ne pas optimiser \u00e0 l'aveugle. Profiler d'abord (cProfile, py-spy).
4. **Code review systematique** : m\u00eame en solo, relire son code 24h apr\u00e8s avant de merger.
5. **Tests d'abord pour les bugs** : tout bug d\u00e9couvert \u2192 \u00e9crire le test qui le reproduit AVANT de fixer.
6. **Ne pas hardcoder** : tout ce qui peut changer (URL, cl\u00e9, message) en variable d'env ou config.
7. **\u00c9viter la dette technique \"plus tard\"** : si tu vois un TODO en codant, soit tu le fais maintenant, soit tu cr\u00e9es une t\u00e2che dans ce plan.

---

*Fin du plan. Ce document doit \u00eatre revu mensuellement et mis \u00e0 jour avec les priorit\u00e9s \u00e9volutives.*
