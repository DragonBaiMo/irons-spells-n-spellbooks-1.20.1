import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const skillDocsDir = path.resolve(__dirname, '../../skill-docs');
const outputDir = path.resolve(__dirname, '../public');
const outputFile = path.join(outputDir, 'skills.json');

// Ensure output dir exists
if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
}

const skills = [];

try {
    const files = fs.readdirSync(skillDocsDir).filter(f => f.endsWith('.md'));

    for (const file of files) {
        const content = fs.readFileSync(path.join(skillDocsDir, file), 'utf-8');
        
        // Parse Header: ### Name (id)
        const headerMatch = content.match(/###\s+(\w+)\s+\(([^)]+)\)/);
        if (!headerMatch) continue;
        
        const displayName = headerMatch[1];
        const fullId = headerMatch[2];
        const commandId = fullId.split(':')[1] || fullId;
        
        // Parse Description
        // Look for **说明**: ...
        const descMatch = content.match(/\*\*说明\*\*:\s*([\s\S]*?)$/);
        const description = descMatch ? descMatch[1].trim() : '';
        
        // Parse Parameters Table
        const params = [];
        const lines = content.split('\n');
        let inTable = false;
        
        for (const line of lines) {
            if (line.trim().startsWith('| 参数名 |')) {
                inTable = true;
                continue;
            }
            if (inTable && line.trim().startsWith('|---')) {
                continue;
            }
            if (inTable && line.trim().startsWith('|')) {
                const parts = line.split('|').map(s => s.trim()).filter(s => s);
                if (parts.length >= 4) {
                    // Name column might have aliases: "manaCost / baseManaCost"
                    const namePart = parts[0];
                    const aliases = namePart.split('/').map(s => s.trim());
                    // Assuming the last one is the key as per PRD hint
                    const key = aliases[aliases.length - 1]; 
                    
                    const type = parts[1];
                    const defaultValue = parts[2];
                    const desc = parts[3];
                    
                    params.push({
                        key,
                        aliases: aliases.slice(0, -1),
                        type,
                        defaultValue,
                        description: desc
                    });
                }
            } else if (inTable && line.trim() === '') {
                // Empty line might end table, but markdown tables can be adjacent to text.
                // Usually a blank line ends it.
                inTable = false;
            }
        }
        
        skills.push({
            displayName,
            fullId,
            commandId,
            description,
            params
        });
    }

    fs.writeFileSync(outputFile, JSON.stringify(skills, null, 2));
    console.log(`Generated ${skills.length} skills to ${outputFile}`);
} catch (e) {
    console.error("Error generating skills:", e);
    process.exit(1);
}
